// src/main/java/com/yong2gether/ywave/bookmark/repository/BookmarkGroupRepository.java
package com.yong2gether.ywave.bookmark.repository;

import com.yong2gether.ywave.bookmark.domain.BookmarkGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkGroupRepository extends JpaRepository<BookmarkGroup, Long> {
    Optional<BookmarkGroup> findByUserIdAndIsDefaultTrue(Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
}
