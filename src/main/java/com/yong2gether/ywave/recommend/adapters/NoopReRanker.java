package com.yong2gether.ywave.recommend.adapters;

import com.yong2gether.ywave.recommend.dto.StoreCandidate;
import com.yong2gether.ywave.recommend.ports.LlmReRanker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnMissingBean(name = "openAiReRanker")
public class NoopReRanker implements LlmReRanker {
    @Override
    public List<StoreCandidate> rerankWithReasons(List<StoreCandidate> candidates, int k, List<String> preferredCategoryNames) {
        String catHint = (preferredCategoryNames == null || preferredCategoryNames.isEmpty())
                ? "선호 지역 내 추천 가맹점"
                : String.join(", ", preferredCategoryNames) + " 선호에 맞춘 추천";
        return candidates.stream().limit(k).map(c ->
                new StoreCandidate(
                        c.id(), c.name(), c.roadAddr(), c.sigungu(),
                        c.lng(), c.lat(), c.popularityScore(), catHint
                )
        ).toList();
    }
}
