package com.example.demo;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.listener.RecordInterceptor;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

class TracingInterceptor<T> implements RecordInterceptor<String, T> {

    private static final String TRACEPARENT_HEADER = "traceparent";
    private static final ThreadLocal<Scope> CURRENT_SCOPE = new ThreadLocal<>();

    private final Tracer tracer;

    TracingInterceptor() {
        this.tracer = GlobalOpenTelemetry.getTracer("kafka-consumer");
    }

    @Override
    public @Nullable ConsumerRecord<String, T> intercept(ConsumerRecord<String, T> record, Consumer<String, T> consumer) {
        String traceparent = extractTraceparent(record);
        Span span = startSpan(record.topic(), traceparent);
        CURRENT_SCOPE.set(span.makeCurrent());
        return record;
    }

    @Override
    public void afterRecord(ConsumerRecord<String, T> record, Consumer<String, T> consumer) {
        Scope scope = CURRENT_SCOPE.get();
        if (scope != null) {
            scope.close();
            CURRENT_SCOPE.remove();
        }
    }

    private @Nullable String extractTraceparent(ConsumerRecord<String, T> record) {
        Header header = record.headers().lastHeader(TRACEPARENT_HEADER);
        if (header != null && header.value() != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return null;
    }

    private Span startSpan(String topic, @Nullable String traceparent) {
        Context parentContext = parseTraceparent(traceparent);
        return tracer.spanBuilder("kafka consume " + topic)
                     .setSpanKind(SpanKind.CONSUMER)
                     .setParent(parentContext)
                     .startSpan();
    }

    private Context parseTraceparent(@Nullable String traceparent) {
        if (traceparent == null || traceparent.isEmpty()) {
            return Context.current();
        }

        String[] parts = traceparent.split("-");
        if (parts.length < 4) {
            return Context.current();
        }

        String traceId = parts[1];
        String spanId = parts[2];
        String traceFlags = parts[3];

        SpanContext spanContext = SpanContext.createFromRemoteParent(
                traceId,
                spanId,
                TraceFlags.fromHex(traceFlags, 0),
                TraceState.getDefault()
        );

        return Context.current().with(Span.wrap(spanContext));
    }
}
