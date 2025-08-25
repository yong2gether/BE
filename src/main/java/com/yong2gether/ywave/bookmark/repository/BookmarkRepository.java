// src/main/java/com/yong2gether/ywave/bookmark/repository/BookmarkRepository.java
package com.yong2gether.ywave.bookmark.repository;

import com.yong2gether.ywave.bookmark.domain.Bookmark;
import com.yong2gether.ywave.bookmark.repository.projection.BookmarkFlatView;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;              // [추가]
import java.util.Collection;                // [추가]
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    /**
     * 유저의 모든 북마크 그룹과 해당 그룹의 매장들을 납작하게 조회 (리스트 화면용)
     */
    @Query(value = """
      select
        g.id                               as groupId,
        g.name                             as groupName,
        g.is_default                       as isDefault,
        s.id                               as storeId,
        s.name                             as storeName,
        (
          select string_agg(distinct c.name, ',')
          from core.store_category sc
          join core.category c on c.id = sc.category_id
          where sc.store_id = s.id
        )                                   as category,
        coalesce(s.road_addr, s.lotno_addr) as roadAddress,
        case when s.geom is not null then ST_Y(s.geom) else null end as lat,
        case when s.geom is not null then ST_X(s.geom) else null end as lng,
        s.phone                            as phone,
        coalesce(s.thumbnail_url, '')      as thumbnailUrl,
        cast(null as float8)               as rating,
        cast(null as int)                  as reviewCount
      from core.bookmark_group g
        left join core.bookmark b on b.bookmark_group_id = g.id
        left join core.stores   s on s.id = b.store_id
      where g.user_id = :userId
      order by 
        g.is_default desc,            -- 기본 그룹 우선
        g.created_at asc,             -- 그 외 그룹은 생성순
        b.created_at desc nulls last, -- 그룹 내 매장은 최신 북마크순
        s.name asc                    -- 동순위 보정
    """, nativeQuery = true)
    List<BookmarkFlatView> findAllGroupsWithStores(@Param("userId") Long userId);

    /** 특정 그룹에 속한 북마크 일괄 삭제 */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByGroup_Id(@Param("groupId") Long groupId);

    /** 북마크 여부 판단 (유저+매장) */
    boolean existsByUser_IdAndStore_Id(Long userId, Long storeId);

    /** (중복일 때 기존 엔티티 필요 시) 유저+매장 단건 조회 */
    Optional<Bookmark> findByUser_IdAndStore_Id(Long userId, Long storeId);

    /** (취소) 유저+매장 기준으로 삭제 → 영향 행 수 반환(200+JSON 응답용) */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    int deleteByUser_IdAndStore_Id(Long userId, Long storeId);

    /** (그룹 화면 등) 유저의 특정 그룹에 속한 북마크 목록 - 최신순 */
    List<Bookmark> findByUser_IdAndGroup_IdOrderByCreatedAtDesc(Long userId, Long groupId);

    /** 그룹 아이콘 URL 조회 (보조용) */
    @Query("select bg.iconUrl from BookmarkGroup bg where bg.id = :groupId")
    String findIconUrlByGroupId(@Param("groupId") Long groupId);

    /** 그룹에 포함된 매장 ID들 조회 */
    @Query("select b.store.id from Bookmark b where b.group.id = :groupId")
    List<Long> findStoreIdsByGroupId(@Param("groupId") Long groupId);


    // ===========================
    // 여기부터 [추가]: 사용자 북마크 목록/배치 체크
    // ===========================

    /**
     * [신규] 사용자 전체 북마크(최신순).
     * GET /api/v1/users/{userId}/bookmarks 에서 그대로 쓰기 좋은 가벼운 Projection.
     */
    @Query("""
        select b.id       as bookmarkId,
               b.store.id as storeId,
               b.group.id as bookmarkGroupId,
               b.createdAt as createdAt
        from Bookmark b
        where b.user.id = :userId
        order by b.createdAt desc
    """)
    List<UserBookmarkView> findUserBookmarks(@Param("userId") Long userId);

    interface UserBookmarkView {
        Long getBookmarkId();
        Long getStoreId();
        Long getBookmarkGroupId();
        LocalDateTime getCreatedAt();
    }

    /**
     * [신규] 화면에 보이는 storeIds만 한 번에 체크 (N+1 방지용).
     * bookmarked 여부만 필요하면 이걸로 충분.
     */
    @Query("""
        select b.store.id
        from Bookmark b
        where b.user.id = :userId
          and b.store.id in :storeIds
    """)
    List<Long> findBookmarkedStoreIdsIn(@Param("userId") Long userId,
                                        @Param("storeIds") Collection<Long> storeIds);

    /**
     * [신규] 그룹 ID까지 함께(목록 카드에 그룹 뱃지 등 표시용).
     */
    @Query("""
        select b.store.id as storeId,
               b.group.id as bookmarkGroupId
        from Bookmark b
        where b.user.id = :userId
          and b.store.id in :storeIds
    """)
    List<BookmarkedInfo> findBookmarkedInfosIn(@Param("userId") Long userId,
                                               @Param("storeIds") Collection<Long> storeIds);

    interface BookmarkedInfo {
        Long getStoreId();
        Long getBookmarkGroupId();
    }
}
