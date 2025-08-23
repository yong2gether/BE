package com.yong2gether.ywave.preference.domain;

import java.util.Arrays;

public enum CategoryType {
    FOOD("음식점"),
    CAFE("카페"),
    MOVIE("영화공연"),
    HOSPITAL("의료기관"),
    MART("마트슈퍼"),
    EDUCATION("교육문구"),
    HOTEL("숙박"),
    DAILY("생활편의"),
    FASHION("의류잡화"),
    SPORTS("체육시설"),
    GAS("주유소"),
    ETC("기타");

    private final String kor;

    CategoryType(String kor) { this.kor = kor; }

    public String kor() { return kor; }

    public static CategoryType from(String value) {
        if (value == null) throw new IllegalArgumentException("카테고리가 비어있습니다.");
        String v = value.trim();
        // 한글 매칭
        for (CategoryType ct : values()) {
            if (ct.kor.equals(v)) return ct;
        }
        // 영문(enum name) 매칭
        return Arrays.stream(values())
                .filter(ct -> ct.name().equalsIgnoreCase(v))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 카테고리: " + value));
    }
}
