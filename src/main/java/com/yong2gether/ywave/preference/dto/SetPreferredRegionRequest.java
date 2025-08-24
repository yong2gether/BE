package com.yong2gether.ywave.preference.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SetPreferredRegionRequest {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "경기도")
    private String sido;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "용인시 수지구")
    private String sigungu;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "죽전동") // null이면 시군구 전체
    private String dong;
}
