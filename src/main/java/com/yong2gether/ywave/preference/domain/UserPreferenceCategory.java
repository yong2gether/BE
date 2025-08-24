package com.yong2gether.ywave.preference.domain;

import com.yong2gether.ywave.store.domain.Category;
import com.yong2gether.ywave.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "preference_category",
        schema = "core",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pref_user_category",
                        columnNames = {"user_id", "category_id"}
                )
        },
        indexes = {
                @Index(name = "ix_preference_category_user_id", columnList = "user_id"),
                @Index(name = "ix_preference_category_category_id", columnList = "category_id")
        }
)
public class UserPreferenceCategory {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private UserPreferenceCategory(User user, Category category) {
        this.user = user;
        this.category = category;
    }

    public static UserPreferenceCategory of(User user, Category category) {
        return new UserPreferenceCategory(user, category);
    }
}
