package com.example.demo;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import io.opentelemetry.api.trace.Span;

@KafkaListener(topics = "someTopic", groupId = "someGroup")
@Component
class SpanLoggingListener {

    private static final Logger LOG = LoggerFactory.getLogger(SpanLoggingListener.class);

    @KafkaHandler(isDefault = true)
    void handle(ConsumerRecord<String, String> record) {
        Span span = Span.current();
        LOG.info("Traceparent: %s. Trace id: %s.".formatted(
                new String(record.headers().lastHeader("traceparent").value()),
                span.getSpanContext().getTraceId()
        ));
    }
}
