package service2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SpotifyService {

    public String findSpotifyId(String name) throws IOException, InterruptedException {
        String rapidAPIKey = "39ceec77d2mshc70076fe5410d5fp1aad86jsn805d43494267";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://spotify23.p.rapidapi.com/search/?q=" + encodeValue(name) + "&type=tracks&offset=0&limit=1&numberOfTopResults=1"))
                .header("X-RapidAPI-Key", rapidAPIKey)
                .header("X-RapidAPI-Host", "spotify23.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        String responseString = response.body();
        return findSpotifyIdFromResponse(responseString);
    }

    private String encodeValue(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String findSpotifyIdFromResponse(String responseString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseString);
        return jsonNode.get("tracks").get("items").iterator().next().get("data").get("id").asText();
    }
}
