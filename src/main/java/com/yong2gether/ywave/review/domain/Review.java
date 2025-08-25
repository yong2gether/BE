package com.yong2gether.ywave.review.domain;

import com.yong2gether.ywave.global.domain.BaseTime;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review", schema = "core")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(name="store_id", nullable = false)
    private Long storeId;

    @Column(name="img_urls", columnDefinition = "text[]")
    @Builder.Default
    private List<String> imgUrls = new ArrayList<>();

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private Double rating;

    public void addImageUrl(String url) {
        if (imgUrls.size() < 5) {
            imgUrls.add(url);
        }
    }

    public void clearImages() {
        imgUrls.clear();
    }
}
