package com.yong2gether.ywave.store.dto;

import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class StoreCandidateRow {
    private Long id;
    private String name;
    private String sido;
    private String sigungu;
    private String roadAddr;
    private String sectorRaw;
    private double lng;
    private double lat;
    private double popularityScore;
}