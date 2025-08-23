package com.yong2gether.ywave.preference.repository;

import com.yong2gether.ywave.preference.domain.CategoryType;
import com.yong2gether.ywave.preference.domain.UserPreferenceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserPreferenceCategoryRepository extends JpaRepository<UserPreferenceCategory, Long> {

    List<UserPreferenceCategory> findByUser_Id(Long userId);

    boolean existsByUser_IdAndCategory(Long userId, CategoryType category);

    void deleteByUser_IdAndCategoryNotIn(Long userId, Collection<CategoryType> categories);
}
