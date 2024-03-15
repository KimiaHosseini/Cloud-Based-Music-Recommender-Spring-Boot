package service3.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    public void sendMessage(String message, String to) throws UnirestException {
        HttpResponse<JsonNode> request = Unirest.post("https://api.mailgun.net/v3/" + "sandbox13f29f78e0b4441caf77ce054d8149a4.mailgun.org" + "/messages")
			.basicAuth("api", "549be4eb12f090c6555d59e13c9848c6-2c441066-3168c121")
                .queryString("from", "hosseinikimia8@gmail.com")
                .queryString("to", to)
                .queryString("subject", "Song Recommendations")
                .queryString("text", "here are your recommendations: \n" + message)
                .asJson();
    }
}