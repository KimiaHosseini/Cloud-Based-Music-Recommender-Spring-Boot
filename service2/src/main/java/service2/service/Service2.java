package service2.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service2.entity.Request;
import service2.entity.Status;
import service2.repository.RequestRepository;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

@Service
public class Service2 {

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private SpotifyService spotifyService;

    @RabbitListener(queues = "music-requests")
    public void receiveMessage(String message){
        processSong(Long.parseLong(message));
    }

    private void processSong(Long songId) {
        Optional<Request> optionalRequest = requestRepository.findById(songId);
        if(optionalRequest.isEmpty())
            return;
        Request request = optionalRequest.get();
        String songS3URI = request.getSongS3URI();
        File musicFile;
        try {
            musicFile = downloadS3Object(songS3URI);
        }catch (Exception e){
            System.err.println(e.getMessage());
            failure(request);
            return;
        }

        String responseString;
        try {
            responseString = sendShazamReq(musicFile);
        }catch (Exception e){
            System.err.println(e.getMessage());
            failure(request);
            return;
        }

        String name;
        try {
            name = findMusicNameFromShazamResponse(responseString);
        }catch (Exception e){
            System.err.println(e.getMessage());
            failure(request);
            return;
        }

        String spotifyId;
        try {
            spotifyId = spotifyService.findSpotifyId(name);
        }catch (Exception e){
            System.err.println(e.getMessage());
            failure(request);
            return;
        }

        request.setSongId(spotifyId);
        request.setStatus(Status.READY.name());
        requestRepository.save(request);
    }

    private File downloadS3Object(String songS3URI) throws URISyntaxException, IOException {
        URI fileToBeDownloaded = new URI(songS3URI);
        AmazonS3URI s3URI = new AmazonS3URI(fileToBeDownloaded);
        S3Object s3Object = s3Client.getObject(s3URI.getBucket(), s3URI.getKey());
        String filename = "music" + System.currentTimeMillis() + ".mp3";
        File musicFile = new File("./" + filename);
        Files.copy(s3Object.getObjectContent(), musicFile.toPath());
        return musicFile;
    }

    private String sendShazamReq(File musicFile) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://shazam-api-free.p.rapidapi.com/shazam/recognize/");
        httpPost.setHeader("X-RapidAPI-Key", "39ceec77d2mshc70076fe5410d5fp1aad86jsn805d43494267");
        httpPost.setHeader("X-RapidAPI-Host", "shazam-api-free.p.rapidapi.com");

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("upload_file", musicFile, ContentType.MULTIPART_FORM_DATA, musicFile.getName().replaceAll("\\s+",""));

        HttpEntity multipart = builder.build();
        httpPost.setEntity(multipart);

        HttpResponse response = httpClient.execute(httpPost);
        System.out.println(response.getStatusLine());
        HttpEntity responseEntity = response.getEntity();
        String responseString = EntityUtils.toString(responseEntity, "UTF-8");
        httpClient.close();
        return responseString;
    }

    private String findMusicNameFromShazamResponse(String responseString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseString);
        return jsonNode.get("track").get("title").asText();
    }

    private void failure(Request request){
        request.setStatus(Status.FAILURE.name());
        requestRepository.save(request);
    }
}