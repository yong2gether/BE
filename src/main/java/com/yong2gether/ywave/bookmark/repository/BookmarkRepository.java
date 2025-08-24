// src/main/java/com/yong2gether/ywave/bookmark/repository/BookmarkRepository.java
package com.yong2gether.ywave.bookmark.repository;

import com.yong2gether.ywave.bookmark.domain.Bookmark;
import com.yong2gether.ywave.bookmark.repository.projection.BookmarkFlatView;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query(value = """
      select
        g.id                              as groupId,
        g.name                            as groupName,
        g.is_default                      as isDefault,
        s.id                              as storeId,
        s.name                            as storeName,
        (
          select string_agg(distinct c.name, ',')
          from core.store_category sc
          join core.category c on c.id = sc.category_id
          where sc.store_id = s.id
        )                                  as category,
        coalesce(s.road_addr, s.lotno_addr) as roadAddress,
        case when s.geom is not null then ST_Y(s.geom) else null end as lat,
        case when s.geom is not null then ST_X(s.geom) else null end as lng,
        s.phone                           as phone,
        coalesce(s.thumbnail_url, '')     as thumbnailUrl,
        cast(null as float8)              as rating,
        cast(null as int)                 as reviewCount
      from core.bookmark_group g
        left join core.bookmark b on b.bookmark_group_id = g.id
        left join core.stores   s on s.id = b.store_id
      where g.user_id = :userId
      order by 
        g.is_default desc,           
        g.created_at asc,            
        b.created_at desc nulls last,
        s.name asc                   
    """, nativeQuery = true)
    List<BookmarkFlatView> findAllGroupsWithStores(@Param("userId") Long userId);

    boolean existsByUser_IdAndStore_Id(Long userId, Long storeId);

    long deleteByUser_IdAndStore_Id(Long userId, Long storeId);

    @Query(value = """
        select coalesce(g.icon_url, '')
        from core.bookmark_group g
        where g.id = :groupId
        """, nativeQuery = true)
    String findIconUrlByGroupId(@Param("groupId") Long groupId);

    long deleteByGroup_Id(Long groupId);
}
