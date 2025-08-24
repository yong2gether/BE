// src/main/java/com/yong2gether/ywave/bookmark/domain/Bookmark.java
package com.yong2gether.ywave.bookmark.domain;

import com.yong2gether.ywave.global.domain.BaseTime;
import com.yong2gether.ywave.store.domain.Store;
import com.yong2gether.ywave.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = {"group", "user", "store"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(
        name = "bookmark",
        schema = "core",
        uniqueConstraints = {
                // 동일 유저가 같은 가맹점을 중복 북마크하지 못하도록 물리 제약
                @UniqueConstraint(name = "uq_bookmark_user_store", columnNames = {"user_id", "store_id"})
        },
        indexes = {
                @Index(name = "idx_bookmark_user", columnList = "user_id"),
                @Index(name = "idx_bookmark_group", columnList = "bookmark_group_id"),
                @Index(name = "idx_bookmark_store", columnList = "store_id")
        }
)
public class Bookmark extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 그룹은 필수 (기본/지정)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bookmark_group_id", nullable = false)
    private BookmarkGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    /** 생성 편의 메서드 */
    public static Bookmark of(User user, Store store, BookmarkGroup group) {
        return Bookmark.builder()
                .user(user)
                .store(store)
                .group(group)
                .build();
    }

    /** 그룹 변경(이동) */
    public void changeGroup(BookmarkGroup newGroup) {
        this.group = newGroup;
    }
}
