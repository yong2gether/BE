// src/main/java/com/yong2gether/ywave/bookmark/repository/BookmarkGroupRepository.java
package com.yong2gether.ywave.bookmark.repository;

import com.yong2gether.ywave.bookmark.domain.BookmarkGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkGroupRepository extends JpaRepository<BookmarkGroup, Long> {

    // === 연관경로(권장) 메서드들 ===
    Optional<BookmarkGroup> findByIdAndUser_Id(Long id, Long userId);
    Optional<BookmarkGroup> findByUser_IdAndName(Long userId, String name);
    boolean existsByUser_IdAndName(Long userId, String name);
    Optional<BookmarkGroup> findByUser_IdAndIsDefaultTrue(Long userId);

    // === 호환(기존 호출 유지용) 별칭 메서드들 ===
    // Spring Data가 파생쿼리를 만들지 않도록 default로 위 메서드에 위임
    default Optional<BookmarkGroup> findByUserIdAndIsDefaultTrue(Long userId) {
        return findByUser_IdAndIsDefaultTrue(userId);
    }
    default boolean existsByUserIdAndName(Long userId, String name) {
        return existsByUser_IdAndName(userId, name);
    }
}
