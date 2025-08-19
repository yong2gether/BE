package com.yong2gether.ywave.user.domain;

import com.yong2gether.ywave.store.domain.Category;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "preference_category",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_preference_category_user_category", columnNames = {"user_id", "category_id"})
        },
        indexes = {
                @Index(name = "ix_preference_category_user_id", columnList = "user_id"),
                @Index(name = "ix_preference_category_category_id", columnList = "category_id")
        }
)
public class PreferenceCategory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public static PreferenceCategory of(User user, Category category) {
        return PreferenceCategory.builder()
                .user(user)
                .category(category)
                .build();
    }
}