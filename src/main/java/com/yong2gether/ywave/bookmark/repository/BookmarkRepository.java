// src/main/java/com/yong2gether/ywave/bookmark/repository/BookmarkRepository.java
package com.yong2gether.ywave.bookmark.repository;

import com.yong2gether.ywave.bookmark.domain.Bookmark;
import com.yong2gether.ywave.bookmark.repository.projection.BookmarkFlatView;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

// src/main/java/com/yong2gether/ywave/bookmark/repository/BookmarkRepository.java
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("""
      select
        g.id as groupId,
        g.name as groupName,
        g.isDefault as isDefault,
        s.id as storeId,
        s.name as storeName,
        s.category as category,
        s.roadAddress as roadAddress,
        s.lat as lat,
        s.lng as lng,
        s.phone as phone,
        s.rating as rating,
        s.reviewCount as reviewCount
      from BookmarkGroup g
        left join Bookmark b on b.group = g       
        left join b.store s                    
      where g.user.id = :userId
      order by g.isDefault desc, g.name asc, s.name asc
    """)
    List<BookmarkFlatView> findAllGroupsWithStores(@Param("userId") Long userId);
}
