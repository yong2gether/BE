package com.yong2gether.ywave.review.repository;

import com.yong2gether.ywave.review.domain.Review;
import com.yong2gether.ywave.review.repository.projection.ReviewListItemView;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
        select 
            r.id as reviewId,
            s.id as storeId,
            s.name as storeName,
            r.content as content,
            r.rating as rating,
            r.createdAt as createdAt
        from Review r
        join r.store s
        where r.userId = :userId
        order by r.createdAt desc
    """)
    List<ReviewListItemView> findAllByUserId(@Param("userId") Long userId);
}
