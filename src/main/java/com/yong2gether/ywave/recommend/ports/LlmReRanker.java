package com.yong2gether.ywave.recommend.ports;

import com.yong2gether.ywave.recommend.dto.StoreCandidate;
import java.util.List;

public interface LlmReRanker {
    List<StoreCandidate> rerankWithReasons(
            List<StoreCandidate> candidates, int k, List<String> preferredCategoryNames
    );
}
