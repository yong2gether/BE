package com.yong2gether.ywave.preference.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "region_center", schema = "core")
public class RegionCenter {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sido;

    @Column(nullable = false)
    private String sigungu;

    private String dong;              // nullable 허용

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;
}
