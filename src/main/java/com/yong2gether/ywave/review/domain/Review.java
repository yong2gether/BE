package com.yong2gether.ywave.review.domain;

import com.yong2gether.ywave.global.domain.BaseTime;
import com.yong2gether.ywave.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private Double rating;

    @ElementCollection
    @CollectionTable(
            name = "review_image_urls",
            joinColumns = @JoinColumn(name = "review_id")
    )
    @Column(name = "image_url", length = 500)
    private List<String> imageUrls;
}
