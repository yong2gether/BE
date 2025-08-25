// src/main/java/com/yong2gether/ywave/bookmark/domain/BookmarkGroup.java
package com.yong2gether.ywave.bookmark.domain;

import com.yong2gether.ywave.global.domain.BaseTime;
import com.yong2gether.ywave.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter // 마이페이지에서 setName/setIconUrl 호출함 → 필요
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(
        name = "bookmark_group",
        schema = "core",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_bookmark_group_user_name", columnNames = {"user_id", "name"})
        },
        indexes = {
                @Index(name = "idx_bookmark_group_user", columnList = "user_id")
        }
)
public class BookmarkGroup extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    // 마이페이지 코드에서 사용 (builder.iconUrl(...), getIconUrl(), setIconUrl(...))
    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    public static BookmarkGroup create(User user, String name, boolean isDefault) {
        return BookmarkGroup.builder()
                .user(user)
                .name(name)
                .isDefault(isDefault)
                .build();
    }
}
