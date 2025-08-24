// src/main/java/com/yong2gether/ywave/bookmark/service/BookmarkService.java
package com.yong2gether.ywave.bookmark.service;

import com.yong2gether.ywave.bookmark.domain.Bookmark;
import com.yong2gether.ywave.bookmark.domain.BookmarkGroup;
import com.yong2gether.ywave.bookmark.repository.BookmarkGroupRepository;
import com.yong2gether.ywave.bookmark.repository.BookmarkRepository;
import com.yong2gether.ywave.store.domain.Store;
import com.yong2gether.ywave.user.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkGroupRepository bookmarkGroupRepository;

    private static final String DEFAULT_GROUP_NAME = "기본";

    @PersistenceContext
    private EntityManager em;

    /** 북마크 생성 (groupId가 null이면 기본 그룹 자동 생성/할당) */
    @Transactional
    public Long create(Long userId, Long storeId, Long groupId) {
        // 1) 중복 방지 (유저+매장)
        if (bookmarkRepository.existsByUser_IdAndStore_Id(userId, storeId)) {
            return bookmarkRepository.findByUser_IdAndStore_Id(userId, storeId)
                    .map(Bookmark::getId)
                    .orElseThrow();
        }

        // 2) 그룹 결정
        BookmarkGroup group = resolveGroup(userId, groupId);

        // 3) 연관 엔티티 프록시 참조 확보 (불필요한 select 방지)
        User userRef = em.getReference(User.class, userId);
        Store storeRef = em.getReference(Store.class, storeId);

        // 4) 저장
        Bookmark saved = bookmarkRepository.save(Bookmark.of(userRef, storeRef, group));
        return saved.getId();
    }

    /** 북마크 취소 (없어도 조용히 성공 처리 정책) */
    @Transactional
    public void delete(Long userId, Long storeId) {
        if (!bookmarkRepository.existsByUser_IdAndStore_Id(userId, storeId)) {
            return;
        }
        bookmarkRepository.deleteByUser_IdAndStore_Id(userId, storeId);
    }

    /** groupId가 있으면 소유자 검증 뒤 사용, 없으면 기본 그룹 조회/생성 */
    private BookmarkGroup resolveGroup(Long userId, Long groupId) {
        if (groupId != null) {
            return bookmarkGroupRepository.findByIdAndUser_Id(groupId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 그룹을 찾을 수 없습니다."));
        }
        // 기본 그룹 조회 or 생성
        return bookmarkGroupRepository.findByUser_IdAndName(userId, DEFAULT_GROUP_NAME)
                .orElseGet(() -> bookmarkGroupRepository.save(
                        BookmarkGroup.create(
                                em.getReference(User.class, userId),
                                DEFAULT_GROUP_NAME,
                                true
                        )
                ));
    }
}
