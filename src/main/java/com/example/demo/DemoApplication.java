package com.example.demo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.microsoft.applicationinsights.attach.ApplicationInsights;


@SpringBootApplication
@EnableScheduling
public class DemoApplication {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    static void main(String[] args) {
        ApplicationInsights.attach();
        SpringApplication.run(DemoApplication.class, args);
    }

    @Scheduled(fixedDelay = 5000)
    void sendMessage() throws ExecutionException, InterruptedException {
        kafkaTemplate.send(new GenericMessage<>("foobar",
                             Map.of(
                                     "kafka_topic", "someTopic",
                                     "traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01")
                     ))
                     .get();
    }
}
