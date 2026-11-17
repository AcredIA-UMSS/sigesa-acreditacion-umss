package com.umss.sigesa.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

/**
 * Spring Boot 4.1 {@code spring-boot-starter-opentelemetry} creates an OTLP SpanExporter bean
 * that is not injectable as {@link SpanExporter}, and a bare {@link OpenTelemetrySdk} without
 * an {@link SdkTracerProvider}. Without this wiring, Micrometer HTTP observations never become
 * exportable spans for Tempo.
 */
@Configuration
@ConditionalOnClass({SdkTracerProvider.class, OtlpHttpSpanExporter.class})
@ConditionalOnProperty(name = "management.tracing.enabled", havingValue = "true", matchIfMissing = true)
public class OpenTelemetryTracingConfig {

    private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "management.otlp.tracing.export.enabled", havingValue = "true")
    SpanExporter otlpHttpSpanExporter(Environment env) {
        String endpoint = env.getProperty(
                "management.otlp.tracing.endpoint",
                "http://127.0.0.1:4318/v1/traces");
        return OtlpHttpSpanExporter.builder()
                .setEndpoint(endpoint)
                .build();
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "management.otlp.tracing.export.enabled", havingValue = "true")
    SdkTracerProvider sdkTracerProvider(SpanExporter otlpHttpSpanExporter, Environment env) {
        String serviceName = env.getProperty("spring.application.name", "sigesa");
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(SERVICE_NAME, serviceName)));
        return SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(otlpHttpSpanExporter).build())
                .build();
    }

    @Bean(destroyMethod = "close")
    @Primary
    @ConditionalOnProperty(name = "management.otlp.tracing.export.enabled", havingValue = "true")
    OpenTelemetry openTelemetry(SdkTracerProvider sdkTracerProvider) {
        return OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();
    }
}
