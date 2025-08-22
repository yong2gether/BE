package com.yong2gether.ywave.review.domain;

import com.yong2gether.ywave.global.domain.BaseTime;
import com.yong2gether.ywave.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review", schema = "core")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review extends BaseTime { // BaseTime 상속 -> created_at(생성일), updated_at(수정일) 자동 세팅

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 리뷰 pk

    @Column(name="user_id", nullable = false)
    private Long userId; // 작성자 유저 PK : "내"가 쓴 리뷰

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 1000)
    private String content; // 리뷰 내용

    @Column(nullable = false)
    private Double rating; // 별점
}

