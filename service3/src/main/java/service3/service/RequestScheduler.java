package service3.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class RequestScheduler {

    @Autowired
    private Service3 service3;

    @Scheduled(fixedRate = 36) // Runs every hour (3600000 milliseconds)
    public void executeHourlyTask() {
        service3.findRecommendations();
    }
}
