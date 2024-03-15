package service1.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service1.entity.Request;
import service1.entity.Status;
import service1.repository.RequestRepository;
import java.io.File;

@Service
public class Service1 {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private AmazonS3 s3Client;

    private final String bucketName = "p1-s3";

    private final String queueName = "music-requests";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public boolean processRequest(String email, File musicFile) {
        String musicName = musicFile.getName().replaceAll("\\s+","");
        boolean b = uploadFileToS3Bucket(musicName, musicFile);
        if(!b)
            return false;

        Request request = new Request();
        request.setEmail(email);
        request.setStatus(Status.PENDING.name());
        request.setSongS3URI("https://p1-s3.s3.ir-thr-at1.arvanstorage.ir/" + musicName);
        requestRepository.save(request);

        System.out.println("We have received your request");

        publishRequestIdToQueue(request.getId());

        return true;
    }

    private boolean uploadFileToS3Bucket(String key, File file) {
        try {
            s3Client.putObject(new PutObjectRequest(bucketName, key, file));
            return true;
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            return false;
        }
    }

    private void publishRequestIdToQueue(Long requestId) {
        sendMessage(requestId.toString());
    }

    private void sendMessage(String message) {
        rabbitTemplate.convertAndSend("", queueName, message);
    }
}

