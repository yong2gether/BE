package com.yong2gether.ywave.recommend.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yong2gether.ywave.global.ai.AiClient;
import com.yong2gether.ywave.recommend.dto.StoreCandidate;
import com.yong2gether.ywave.recommend.ports.LlmReRanker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Component("openAiReRanker")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix="openai.recommend", name="enabled", havingValue="true")
public class OpenAiReRanker implements LlmReRanker {

    private static final String MODEL = "gpt-5-mini";
    private final AiClient ai;
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public List<StoreCandidate> rerankWithReasons(List<StoreCandidate> candidates, int k, List<String> preferredCategoryNames) {
        try {
            // API 키 없으면 바로 폴백
            if (ai.getApiKey() == null || ai.getApiKey().isBlank()) {
                log.warn("OpenAI API key missing. Skip rerank.");
                return candidates.stream().limit(k).toList();
            }

            // --- 프롬프트에 넣는 후보 개수 캡 ---
            int cap = Math.min(100, Math.max(k * 4, 60));
            List<StoreCandidate> promptCandidates = candidates.stream().limit(cap).toList();

            // --- 프롬프트 ---
            String cats = (preferredCategoryNames == null || preferredCategoryNames.isEmpty())
                    ? "제약 없음"
                    : String.join(", ", preferredCategoryNames);

            String system = """
                You are a Korean local business recommender.
                Pick items that best match the user's preferred categories and promote variety.
                Return ONLY a compact JSON array: [{"id":123,"reason":"..."}].
                """;

            StringBuilder user = new StringBuilder();
            user.append("선호 업종 카테고리: ").append(cats).append("\n");
            user.append("후보 목록(최대 100): 각 항목은 {id, name, pop} 형식.\n");
            for (StoreCandidate c : promptCandidates) {
                user.append(String.format("- {id:%d, name:\"%s\", pop:%.3f}\n",
                        c.id(), safe(c.name()), c.popularityScore()));
            }
            user.append("\n규칙:\n");
            user.append("1) 상호 중복 없이 ").append(k).append("개 선정.\n");
            user.append("2) 선호 카테고리와 관련성 높은 순으로 고르되, 너무 유사한 곳만 몰리지 않게 다양화.\n");
            user.append("3) JSON 배열만 출력. id는 숫자, reason은 18자 내 한글 한줄(카테고리 연관 이유 요약).\n");

            // --- 호출 바디 ---
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            h.setBearerAuth(ai.getApiKey());

            Map<String, Object> body = new HashMap<>();
            body.put("model", MODEL);
            body.put("messages", List.of(
                    Map.of("role","system","content", system),
                    Map.of("role","user","content", user.toString())
            ));
            // 응답 지연/비용 안전장치
            body.put("max_completion_tokens", 256);

            var entity = new HttpEntity<>(body, h);

            // --- 타임아웃/혼잡 대비 재시도 ---
            ResponseEntity<Map> resp = postWithRetry(entity);

            String content = Optional.ofNullable(resp.getBody())
                    .map(b -> (List<Map<String,Object>>) b.get("choices"))
                    .filter(l -> !l.isEmpty())
                    .map(l -> (Map<String,Object>) l.get(0).get("message"))
                    .map(m -> (String) m.get("content"))
                    .orElse(null);

            if (content == null || content.isBlank()) {
                return candidates.stream().limit(k).toList();
            }

            // --- JSON 배열만 추출해서 파싱 ---
            String json = extractJsonArray(content);
            JsonNode arr = om.readTree(json);

            Map<Long, String> wanted = new LinkedHashMap<>();
            for (JsonNode n : arr) {
                if (n.has("id")) {
                    long id = n.get("id").asLong();
                    String reason = n.has("reason") ? n.get("reason").asText() : null;
                    wanted.put(id, reason);
                }
            }

            Map<Long, StoreCandidate> byId = candidates.stream()
                    .collect(Collectors.toMap(StoreCandidate::id, x -> x, (a,b)->a, LinkedHashMap::new));

            List<StoreCandidate> out = new ArrayList<>();
            for (var id : wanted.keySet()) {
                var base = byId.get(id);
                if (base != null) {
                    out.add(new StoreCandidate(
                            base.id(), base.name(), base.roadAddr(), base.sigungu(),
                            base.lng(), base.lat(), base.popularityScore(), wanted.get(id)
                    ));
                }
                if (out.size() == k) break;
            }

            // 부족하면 채우기
            if (out.size() < k) {
                for (StoreCandidate c : candidates) {
                    if (out.stream().noneMatch(x -> x.id().equals(c.id()))) {
                        out.add(new StoreCandidate(
                                c.id(), c.name(), c.roadAddr(), c.sigungu(),
                                c.lng(), c.lat(), c.popularityScore(), "선호에 맞춘 보완 추천"
                        ));
                        if (out.size()==k) break;
                    }
                }
            }
            return out;

        } catch (Exception e) {
            log.warn("OpenAI rerank failed: {}", e.toString());
            return candidates.stream().limit(k).toList();
        }
    }

    // --- 재시도 래퍼 (429/5xx/타임아웃만 재시도) ---
    private ResponseEntity<Map> postWithRetry(HttpEntity<?> entity) {
        int[] backoffMs = {600, 1200, 2400};
        for (int i = 0; i < backoffMs.length; i++) {
            try {
                return ai.getRestTemplate().postForEntity(ai.chatCompletionsUrl(), entity, Map.class);
            } catch (ResourceAccessException e) {
                if (i == backoffMs.length - 1) throw e;
                sleep(backoffMs[i]);
            } catch (HttpStatusCodeException e) {
                int code = e.getStatusCode().value();
                boolean retryable = (code == 429) || e.getStatusCode().is5xxServerError();
                if (!retryable || i == backoffMs.length - 1) throw e;
                sleep(backoffMs[i]);
            }
        }
        throw new IllegalStateException("unreachable");
    }

    private void sleep(int baseMs) {
        try { Thread.sleep(baseMs + ThreadLocalRandom.current().nextInt(200)); }
        catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    // 모델이 코드블록/설명 섞어서 줄 때 JSON 배열만 뽑아내기
    private String extractJsonArray(String raw) {
        if (raw == null) return "[]";
        String s = raw.trim();
        if (s.startsWith("```")) {
            int l = s.indexOf('['), r = s.lastIndexOf(']');
            if (l >= 0 && r >= l) return s.substring(l, r + 1);
        }
        if (s.startsWith("[")) return s;
        int l = s.indexOf('['), r = s.lastIndexOf(']');
        if (l >= 0 && r >= l) return s.substring(l, r + 1);
        return "[]";
    }

    private String safe(String s) { return s == null ? "" : s.replace("\"","\\\""); }
}
