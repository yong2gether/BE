package com.yong2gether.ywave.store.repository;

import com.yong2gether.ywave.store.domain.StoreCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreCategoryRepository extends JpaRepository<StoreCategory, Long> {
}
