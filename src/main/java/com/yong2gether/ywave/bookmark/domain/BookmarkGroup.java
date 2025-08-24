// src/main/java/com/yong2gether/ywave/bookmark/domain/BookmarkGroup.java
package com.yong2gether.ywave.bookmark.domain;

import com.yong2gether.ywave.global.domain.BaseTime;
import com.yong2gether.ywave.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(
        name = "bookmark_group",
        schema = "core",
        uniqueConstraints = {
                // 같은 유저가 같은 이름의 그룹을 중복 생성하지 못하도록 제약
                @UniqueConstraint(name = "uq_bookmark_group_user_name", columnNames = {"user_id", "name"})
        },
        indexes = {
                @Index(name = "idx_bookmark_group_user", columnList = "user_id")
        }
)
public class BookmarkGroup extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 유저의 그룹인지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 그룹 이름 (ex. 기본, 카페, 맛집 등)
    @Column(nullable = false, length = 100)
    private String name;

    // 기본 그룹 여부
    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    /** 편의 생성자 */
    public static BookmarkGroup create(User user, String name, boolean isDefault) {
        return BookmarkGroup.builder()
                .user(user)
                .name(name)
                .isDefault(isDefault)
                .build();
    }
}
