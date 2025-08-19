package com.yong2gether.ywave.global.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * openai.api-key 값이 있을 때만 Bean을 생성한다.
 * 값이 없으면 Bean 자체가 만들어지지 않아 부팅이 막히지 않는다.
 */
@Component
@ConditionalOnProperty(prefix = "openai", name = "api-key")
public class AiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;  // e.g. https://api.openai.com
    private final String apiKey;   // sk-...

    public AiClient(
            RestTemplateBuilder builder,
            @Value("${openai.base-url:https://api.openai.com}") String baseUrl,
            @Value("${openai.api-key}") String apiKey
    ) {
        // baseUrl 기본값 + trailing slash 제거
        String trimmed = (baseUrl == null || baseUrl.isBlank()) ? "https://api.openai.com" : baseUrl;
        this.baseUrl = trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        this.apiKey = apiKey;

        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();
    }

    public String chatCompletionsUrl() {
        return baseUrl + "/v1/chat/completions";
    }
    public RestTemplate getRestTemplate() { return restTemplate; }
    public String getApiKey() { return apiKey; }
}
