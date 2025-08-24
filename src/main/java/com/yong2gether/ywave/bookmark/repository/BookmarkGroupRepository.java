// src/main/java/com/yong2gether/ywave/bookmark/repository/BookmarkGroupRepository.java
package com.yong2gether.ywave.bookmark.repository;

import com.yong2gether.ywave.bookmark.domain.BookmarkGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkGroupRepository extends JpaRepository<BookmarkGroup, Long> {

    // 권장 메서드 (연관경로)
    Optional<BookmarkGroup> findByIdAndUser_Id(Long id, Long userId);
    Optional<BookmarkGroup> findByUser_IdAndName(Long userId, String name);
    boolean existsByUser_IdAndName(Long userId, String name);
    Optional<BookmarkGroup> findByUser_IdAndIsDefaultTrue(Long userId);

    // 중복 이름 체크 (자신 제외)
    boolean existsByUser_IdAndNameAndIdNot(Long userId, String name, Long idNot);

    // ===== 호환용 별칭 (기존 호출 유지) =====
    default Optional<BookmarkGroup> findByUserIdAndIsDefaultTrue(Long userId) {
        return findByUser_IdAndIsDefaultTrue(userId);
    }
    default boolean existsByUserIdAndName(Long userId, String name) {
        return existsByUser_IdAndName(userId, name);
    }
    default boolean existsByUserIdAndNameAndIdNot(Long userId, String name, Long idNot) {
        return existsByUser_IdAndNameAndIdNot(userId, name, idNot);
    }
}
