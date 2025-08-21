package com.yong2gether.ywave.preference.domain;

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
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_pref_user_category", columnNames = {"user_id", "category"})
        }
)
public class UserPreferenceCategory {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private CategoryType category;

    private UserPreferenceCategory(User user, CategoryType category) {
        this.user = user;
        this.category = category;
    }

    public static UserPreferenceCategory of(User user, CategoryType category) {
        return new UserPreferenceCategory(user, category);
    }
}
