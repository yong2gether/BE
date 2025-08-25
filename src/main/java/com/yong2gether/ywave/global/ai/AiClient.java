package com.yong2gether.ywave.global.ai;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class AiClient {

    @Getter
    @Value("${openai.api-key:${OPENAI_API_KEY:}}")
    private String apiKey;

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String baseUrl; // 사용자가 v1을 빼먹어도 보정해줄 거야.

    @Value("${openai.http.connect-timeout-ms:3000}")
    private int connectTimeoutMs;

    @Value("${openai.http.read-timeout-ms:35000}")
    private int readTimeoutMs;

    private final RestTemplate restTemplate;

    public AiClient(
            @Value("${openai.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${openai.http.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${openai.http.read-timeout-ms:35000}") int readTimeoutMs
    ) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;

        var f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(this.connectTimeoutMs);
        f.setReadTimeout(this.readTimeoutMs);
        this.restTemplate = new RestTemplate(f);

        log.info("AiClient initialized (connectTimeoutMs={}, readTimeoutMs={})",
                this.connectTimeoutMs, this.readTimeoutMs);
        log.info("OpenAI base-url(raw)={}, endpoint(chat)={}", this.baseUrl, chatCompletionsUrl());
    }

    public RestTemplate getRestTemplate() {
        return this.restTemplate;
    }

    public String chatCompletionsUrl() {
        String base = normalizeBaseUrlWithV1(this.baseUrl);
        return base + "/chat/completions";
    }

    // baseUrl이 v1을 포함하지 않으면 /v1을 강제로 붙인다.
    private String normalizeBaseUrlWithV1(String url) {
        if (url == null || url.isBlank()) return "https://api.openai.com/v1";
        String s = url.trim();

        // 끝 슬래시 제거
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);

        // 이미 /v{number} 포함하면 그대로 사용
        // 예: https://api.openai.com/v1  또는 https://foo.bar/v2
        String lower = s.toLowerCase();
        if (lower.matches(".*/v\\d+($|/.*)")) {
            return s;
        }
        // 없으면 /v1 추가
        return s + "/v1";
    }
}
