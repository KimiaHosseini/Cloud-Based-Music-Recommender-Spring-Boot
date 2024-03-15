package service3.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service3.entity.Request;
import service3.entity.Status;
import service3.repository.RequestRepository;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Iterator;
import java.util.List;

@Service
public class Service3 {

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    MailService mailService;

    public void findRecommendations(){
        List<Request> requests = requestRepository.findByStatus(Status.READY.name());

        for (Request request: requests){
            String songId = request.getSongId();
            try {
                mailService.sendMessage(getRecommendationsFromSpotify(songId), request.getEmail());
            }catch (Exception e){
                System.err.println(e.getMessage());
                request.setStatus(Status.FAILURE.name());
                requestRepository.save(request);
                continue;
            }
            request.setStatus("done");
            requestRepository.save(request);
        }
    }

    private String getRecommendationsFromSpotify(String songId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://spotify23.p.rapidapi.com/recommendations/?limit=20&seed_tracks=" + songId))
                .header("X-RapidAPI-Key", "39ceec77d2mshc70076fe5410d5fp1aad86jsn805d43494267")
                .header("X-RapidAPI-Host", "spotify23.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        String responseString = response.body();
        return processSpotifyResponse(responseString);
    }

    private String processSpotifyResponse(String responseString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseString);
        Iterator<JsonNode> iterator = jsonNode.get("tracks").iterator();
        StringBuilder res = new StringBuilder();
        while (iterator.hasNext()){
            JsonNode recommendation = iterator.next();
            res.append("name: ").append(recommendation.get("name")).append(" Spotify ID: ").append(recommendation.get("id")).append("\n");
        }
        return res.toString();
    }
}
