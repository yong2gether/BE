package com.yong2gether.ywave.store.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yong2gether.ywave.global.ai.AiClient;
import com.yong2gether.ywave.store.domain.Category;
import com.yong2gether.ywave.store.domain.Store;
import com.yong2gether.ywave.store.domain.StoreCategory;
import com.yong2gether.ywave.store.repository.CategoryRepository;
import com.yong2gether.ywave.store.repository.StoreCategoryRepository;
import com.yong2gether.ywave.store.repository.StoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

@Slf4j
@Service
public class CategoryClassifyService {

    private static final Set<String> ALLOWED =
            Set.of("FOOD","CAFE","MOVIE_SHOW","MEDICAL","MART_SUPER",
                    "EDUCATION_STATIONERY","LODGING","LIVING_CONVENIENCE",
                    "APPAREL_MISC","SPORTS","GAS_STATION","ETC");

    private final AiClient ai;
    private final CategoryRepository categoryRepo;
    private final StoreCategoryRepository storeCategoryRepo;
    private final StoreRepository storeRepo;
    private final CategoryRuleMapper rules;           // ✅ 기존 규칙 매퍼 주입
    private final ObjectMapper om = new ObjectMapper();
    private final String model;

    // 시그니처 → 분류결과 캐시(실행 중 재사용; 요금 절감)
    private final Map<String, MajorSub> signatureCache = new HashMap<>();

    public CategoryClassifyService(
            AiClient ai,
            CategoryRepository categoryRepo,
            StoreCategoryRepository storeCategoryRepo,
            StoreRepository storeRepo,
            CategoryRuleMapper rules,
            @Value("${openai.model:gpt-5-mini}") String model
    ) {
        this.ai = ai;
        this.categoryRepo = categoryRepo;
        this.storeCategoryRepo = storeCategoryRepo;
        this.storeRepo = storeRepo;
        this.rules = rules;
        this.model = (model == null || model.isBlank()) ? "gpt-5-mini" : model;
    }

    // ---------- 유틸 ----------
    private static String nz(String s) { return s == null ? "" : s; }
    private static String signatureOf(Store s) {
        return (nz(s.getSectorRaw()) + "|" + nz(s.getMainPrdRaw())).toLowerCase();
    }
    private static String extractJson(String content) {
        if (content == null) return null;
        content = content.trim();
        if (content.startsWith("```")) {
            int s = content.indexOf('{');
            int e = content.lastIndexOf('}');
            if (s >= 0 && e > s) return content.substring(s, e + 1);
        }
        int s = content.indexOf('{');
        int e = content.lastIndexOf('}');
        if (s >= 0 && e > s) return content.substring(s, e + 1);
        return content;
    }

    // ---------- 저장은 건별 커밋 ----------
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upsertOne(Long storeId, Category cat, String sub) {
        var existing = storeCategoryRepo.findById(storeId);
        if (existing.isPresent()) {
            var sc = existing.get();
            sc.setCategory(cat);
            sc.setSubcategory(sub);
            storeCategoryRepo.save(sc);
        } else {
            storeCategoryRepo.save(new StoreCategory(storeId, cat, sub));
        }
    }

    // ---------- OpenAI 호출 (temperature 없음) ----------
    private MajorSub callOpenAi(String sys, String user) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(ai.getApiKey());

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", sys),
                        Map.of("role", "user", "content", user)
                )
        );

        var entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> res =
                ai.getRestTemplate().postForEntity(ai.chatCompletionsUrl(), entity, Map.class);

        Map<?, ?> root = res.getBody();
        if (root == null) throw new IllegalStateException("OpenAI null body");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) root.get("choices");
        if (choices == null || choices.isEmpty())
            throw new IllegalStateException("OpenAI choices empty");

        @SuppressWarnings("unchecked")
        Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
        String content = msg == null ? null : String.valueOf(msg.get("content"));
        String json = extractJson(content);

        JsonNode node = om.readTree(json);
        String major = node.path("major").asText(null);
        String sub = node.path("sub").asText("").trim();
        if (sub.isBlank()) sub = null;

        if (major == null) major = "ETC";
        major = major.trim().toUpperCase();
        if (!ALLOWED.contains(major)) major = "ETC";

        return new MajorSub(major, sub);
    }

    // ---------- 행(스토어) 단위 분류 (기존 all-once에서 호출) ----------
    public int classifyAndUpsert(List<Store> stores) {
        if (stores == null || stores.isEmpty()) return 0;

        int ok = 0, seen = 0;

        for (Store s : stores) {
            if (storeCategoryRepo.findById(s.getId()).isPresent()) continue;

            try {
                // 1) 룰 기반 시도 (요금 0원)
                String majorFromRule = rules.mapMajor(s.getName(), s.getSectorRaw(), s.getMainPrdRaw());
                MajorSub ms;
                if (majorFromRule != null) {
                    ms = new MajorSub(majorFromRule, null);
                } else {
                    // 2) AI (최후 수단)
                    String sys = """
                        당신은 상점 분류기입니다.
                        다음 12개 대분류 중 정확히 1개를 선택하고, 소분류는 한국어 키워드 1~3어로 요약하세요.
                        대분류 키(반드시 이 중 하나):
                        FOOD, CAFE, MOVIE_SHOW, MEDICAL, MART_SUPER, EDUCATION_STATIONERY,
                        LODGING, LIVING_CONVENIENCE, APPAREL_MISC, SPORTS, GAS_STATION, ETC
                        출력은 JSON 한 줄만: {"major":"<위 키 중 하나>", "sub":"<소분류(없으면 빈 문자열)>"}
                        """;
                    String user = "상호명: " + s.getName()
                            + "\n업종: " + nz(s.getSectorRaw())
                            + "\n주요상품: " + nz(s.getMainPrdRaw());
                    ms = callOpenAi(sys, user);
                }

                Category cat = categoryRepo.findByName(ms.major())
                        .orElseGet(() -> categoryRepo.findByName("ETC").orElseThrow());

                upsertOne(s.getId(), cat, ms.sub());
                ok++;

            } catch (HttpClientErrorException e) {
                log.warn("OpenAI HTTP {} storeId={} name='{}' body={}",
                        e.getStatusCode(), s.getId(), s.getName(), e.getResponseBodyAsString());
            } catch (Exception e) {
                log.warn("classify failed storeId={} name='{}' cause={}",
                        s.getId(), s.getName(), e.toString());
            } finally {
                seen++;
                if (seen % 100 == 0) log.info("classify progress: {} / {}", seen, stores.size());
            }
        }
        return ok;
    }

    /** (기존) 미분류 전체를 행 단위로 끝까지 돌림 */
    public int classifyAllOnce(int batchSize, long sleepMillisBetweenCalls) {
        int totalOk = 0;
        long afterId = 0L;

        while (true) {
            List<Store> batch = storeRepo.findUncategorizedAfter(afterId, batchSize);
            if (batch.isEmpty()) break;

            totalOk += classifyAndUpsert(batch);
            afterId = batch.get(batch.size() - 1).getId();

            if (sleepMillisBetweenCalls > 0) {
                try { Thread.sleep(sleepMillisBetweenCalls); } catch (InterruptedException ignore) {}
            }
        }
        return totalOk;
    }

    // ---------- 시그니처 단위 분류 (요금 절감 핵심 경로) ----------
    private MajorSub decideSignature(String sig) throws Exception {
        // 0) 런타임 캐시
        MajorSub cached = signatureCache.get(sig);
        if (cached != null) return cached;

        // 1) 샘플 1건 가져오기
        List<Store> sample = storeRepo.findStoresBySignature(sig, 1);
        if (sample.isEmpty()) {
            MajorSub ms = new MajorSub("ETC", null);
            signatureCache.put(sig, ms);
            return ms;
        }
        Store s = sample.get(0);

        // 2) 우선 룰 적용 (무료)
        String majorFromRule = rules.mapMajor(s.getName(), s.getSectorRaw(), s.getMainPrdRaw());
        MajorSub ms;
        if (majorFromRule != null) {
            ms = new MajorSub(majorFromRule, null);
        } else {
            // 3) 룰로 못하면 그때만 AI 1회
            String sys = """
                당신은 상점 분류기입니다.
                다음 12개 대분류 중 정확히 1개를 선택하고, 소분류는 한국어 키워드 1~3어로 요약하세요.
                대분류 키(반드시 이 중 하나):
                FOOD, CAFE, MOVIE_SHOW, MEDICAL, MART_SUPER, EDUCATION_STATIONERY,
                LODGING, LIVING_CONVENIENCE, APPAREL_MISC, SPORTS, GAS_STATION, ETC
                출력은 JSON 한 줄만: {"major":"<위 키 중 하나>", "sub":"<소분류(없으면 빈 문자열)>"}
                """;
            String user = "상호명: " + s.getName()
                    + "\n업종: " + nz(s.getSectorRaw())
                    + "\n주요상품: " + nz(s.getMainPrdRaw());
            ms = callOpenAi(sys, user);
        }

        signatureCache.put(sig, ms);
        return ms;
    }

    private int applySignatureToStores(String sig, MajorSub ms, int applyBatch) {
        int applied = 0;
        Category cat = categoryRepo.findByName(ms.major())
                .orElseGet(() -> categoryRepo.findByName("ETC").orElseThrow());

        while (true) {
            List<Store> batch = storeRepo.findStoresBySignature(sig, applyBatch);
            if (batch.isEmpty()) break;

            for (Store s : batch) {
                try {
                    if (storeCategoryRepo.findById(s.getId()).isPresent()) continue;
                    upsertOne(s.getId(), cat, ms.sub());
                    applied++;
                } catch (Exception e) {
                    log.warn("apply failed storeId={} sig={} cause={}", s.getId(), sig, e.toString());
                }
            }
        }
        return applied;
    }

    /** 미분류 시그니처 상위 N개 처리 (시그니처당 최대 1회 AI 호출) */
    public int classifyAllBySignature(int sigLimit, int applyBatch) {
        int totalApplied = 0;

        var sigRows = storeRepo.findUncategorizedSignatures(sigLimit);
        log.info("signature candidates: {}", sigRows.size());

        for (var row : sigRows) {
            String sig = row.getSig();
            try {
                MajorSub ms = decideSignature(sig);           // 룰 → (필요 시) AI 1회
                int applied = applySignatureToStores(sig, ms, applyBatch);
                totalApplied += applied;
                log.info("sig='{}' -> {}:{} applied {}", sig, ms.major(), ms.sub(), applied);
            } catch (Exception e) {
                log.warn("signature decide/apply failed sig={} cause={}", sig, e.toString());
            }
        }
        return totalApplied;
    }

    // ---------- DTO ----------
    private record MajorSub(String major, String sub) {}
}