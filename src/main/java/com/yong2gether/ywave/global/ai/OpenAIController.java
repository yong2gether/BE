package com.yong2gether.ywave.global.ai;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class OpenAIController {

    private static final String MODEL = "gpt-5-mini"; // 모델 고정

    private final Optional<AiClient> aiClientOpt;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok(aiClientOpt.isPresent() ? "ai:ready" : "ai:disabled");
    }

    @Operation(summary = "ChatGPT 연결 테스트 API", description = "특정 GPT 모델 연결이 잘 되는지 프롬프르토 간단한 테스트를 하는 API입니다.")
    @PostMapping("/echo")
    public ResponseEntity<?> echo(@RequestBody(required = false) EchoRequest req) {
        var ai = aiClientOpt.orElseThrow(() -> new IllegalStateException("AI client disabled"));

        String prompt = (req != null && StringUtils.hasText(req.prompt)) ? req.prompt : "say 'pong'";
        log.info("OPENAI key len={}, model={}", ai.getApiKey() == null ? 0 : ai.getApiKey().length(), MODEL);

        // 1) 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(ai.getApiKey());

        // 2) 본문 (temperature 등 불필요 파라미터 전송 금지)
        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        try {
            var entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> resp =
                    ai.getRestTemplate().postForEntity(ai.chatCompletionsUrl(), entity, Map.class);

            Map<String, Object> root = resp.getBody();
            String modelResp = root == null ? null : (String) root.get("model");
            String content = null;
            if (root != null) {
                var choices = (List<Map<String, Object>>) root.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    var msg = (Map<String, Object>) choices.get(0).get("message");
                    if (msg != null) content = (String) msg.get("content");
                }
            }
            return ResponseEntity.ok(new ChatTestResponse(
                    modelResp != null ? modelResp : MODEL,
                    content
            ));

        } catch (HttpClientErrorException e) {
            log.warn("OpenAI error {} BAD_REQUEST: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ErrorResponse(e.getStatusCode().value(), e.getResponseBodyAsString()));
        } catch (Exception e) {
            log.error("OpenAI call failed", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponse(502, "upstream_error: " + e.getMessage()));
        }
    }

    // dto
    public record EchoRequest(String prompt) { }
    public record ChatTestResponse(String model, String content) { }
    public record ErrorResponse(int status, String message) { }
}
