// src/main/java/com/yong2gether/ywave/bookmark/repository/BookmarkRepository.java
package com.yong2gether.ywave.bookmark.repository;

import com.yong2gether.ywave.bookmark.domain.Bookmark;
import com.yong2gether.ywave.bookmark.repository.projection.BookmarkFlatView;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
    void deleteByGroup_Id(Long groupId);

    /** 북마크 여부 판단 (유저+매장) */
    boolean existsByUser_IdAndStore_Id(Long userId, Long storeId);

    /** (중복일 때 기존 엔티티 필요 시) 유저+매장 단건 조회 */
    Optional<Bookmark> findByUser_IdAndStore_Id(Long userId, Long storeId);

    /** (취소) 유저+매장 기준으로 삭제 */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByUser_IdAndStore_Id(Long userId, Long storeId);

    /** (그룹 화면 등) 유저의 특정 그룹에 속한 북마크 목록 - 최신순 */
    List<Bookmark> findByUser_IdAndGroup_IdOrderByCreatedAtDesc(Long userId, Long groupId);

    /** 그룹 아이콘 URL 조회 (보조용) */
    @Query("select bg.iconUrl from BookmarkGroup bg where bg.id = :groupId")
    String findIconUrlByGroupId(@Param("groupId") Long groupId);

    /** 그룹에 포함된 매장 ID들 조회 */
    @Query("select b.store.id from Bookmark b where b.group.id = :groupId")
    List<Long> findStoreIdsByGroupId(@Param("groupId") Long groupId);
}
