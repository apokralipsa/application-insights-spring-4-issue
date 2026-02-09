package com.example.demo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.kafka.autoconfigure.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.ContainerCustomizer;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.microsoft.applicationinsights.attach.ApplicationInsights;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    ConcurrentKafkaListenerContainerFactory<String, String> containerFactory;

    static void main(String[] args) {
        ApplicationInsights.attach();
        SpringApplication.run(DemoApplication.class, args);
    }

    @PostConstruct
    void addInterceptor() {
        containerFactory.setRecordInterceptor(new TracingInterceptor<>());
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
