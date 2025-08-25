package com.yong2gether.ywave.recommend.service;

import com.yong2gether.ywave.preference.repository.UserPreferenceCategoryRepository;
import com.yong2gether.ywave.preference.repository.UserPreferenceRegionRepository;
import com.yong2gether.ywave.recommend.dto.RecommendedStore;
import com.yong2gether.ywave.recommend.dto.StoreCandidate;
import com.yong2gether.ywave.recommend.ports.LlmReRanker;
import com.yong2gether.ywave.store.repository.StoreRecommendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserPreferenceRegionRepository regionRepo;
    private final UserPreferenceCategoryRepository categoryRepo;
    private final StoreRecommendRepository storeRepo;
    private final LlmReRanker reRanker; // Noop(기본) 또는 OpenAI(옵션)

    @Value("${recommend.radius-default-m:3000}")
    private int defaultRadiusMeters;

    public List<RecommendedStore> recommendFor(String userEmail, int limit) {
        var region = regionRepo.findLatestByUserEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "선호 지역이 먼저 등록되어야 합니다."));

        var names = categoryRepo.findCategoryNamesByUserEmail(userEmail).stream().distinct().toList();
        var ids   = categoryRepo.findCategoryIdsByUserEmail(userEmail).stream().distinct().toList();

        log.info("recommend user={}, prefCats(names)={}, ids={}", userEmail, names, ids);

        if (names.isEmpty() && ids.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "선호 업종 카테고리가 등록되어야 합니다.");
        }

        int radius = defaultRadiusMeters;
        int oversample = Math.max(limit * 6, 30);

        var rows = !names.isEmpty()
                ? storeRepo.pickWeightedRandomByNames(region.getLng(), region.getLat(), radius,
                names.toArray(String[]::new), names.size(), oversample)
                : storeRepo.pickWeightedRandomByIds(region.getLng(), region.getLat(), radius,
                ids.toArray(Long[]::new), ids.size(), oversample);

        var candidates = rows.stream().map(r -> new StoreCandidate(
                r.getId(), r.getName(), r.getRoadAddr(), r.getSigungu(),
                r.getLng(), r.getLat(), r.getPopularityScore()
        )).toList();

        if (candidates.isEmpty()) return List.of();

        var top = reRanker.rerankWithReasons(candidates, limit, names);

        return top.stream().map(c -> new RecommendedStore(
                c.id(), c.name(), c.roadAddr(), c.sigungu(),
                c.lng(), c.lat(),
                (c.reason() == null || c.reason().isBlank())
                        ? (names.isEmpty() ? "선호 지역 내 추천 가맹점"
                        : String.join(", ", names) + " 선호에 맞춘 추천")
                        : c.reason()
        )).toList();
    }
}
