package com.yong2gether.ywave.preference.repository;

import com.yong2gether.ywave.preference.domain.UserPreferenceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface UserPreferenceCategoryRepository extends JpaRepository<UserPreferenceCategory, Long> {

    List<UserPreferenceCategory> findByUser_Id(Long userId);

    boolean existsByUser_IdAndCategory_Id(Long userId, Long categoryId);

    @Query("""
           select upc
           from UserPreferenceCategory upc
           join upc.user u
           where u.email = :email
           """)
    List<UserPreferenceCategory> findAllByUserEmail(@Param("email") String email);

    // 바로 이름/ID만 뽑는 버전도 제공
    @Query("""
           select c.name
           from UserPreferenceCategory upc
           join upc.user u
           join upc.category c
           where u.email = :email
           """)
    List<String> findCategoryNamesByUserEmail(@Param("email") String email);

    @Query("""
           select c.id
           from UserPreferenceCategory upc
           join upc.user u
           join upc.category c
           where u.email = :email
           """)
    List<Long> findCategoryIdsByUserEmail(@Param("email") String email);

}
