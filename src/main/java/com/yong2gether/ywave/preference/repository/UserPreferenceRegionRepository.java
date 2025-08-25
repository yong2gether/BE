package com.yong2gether.ywave.preference.repository;

import com.yong2gether.ywave.preference.domain.UserPreferenceRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserPreferenceRegionRepository extends JpaRepository<UserPreferenceRegion, Long> {
    List<UserPreferenceRegion> findByUser_Id(Long userId);
    void deleteByUser_Id(Long userId);
    boolean existsByUser_Id(Long userId);

    Optional<UserPreferenceRegion> findOneByUser_Id(Long userId);
    @Query("""
           select pr
           from UserPreferenceRegion pr
           join pr.user u
           where u.email = :email
           order by pr.id desc
           """)
    Optional<UserPreferenceRegion> findLatestByUserEmail(@Param("email") String email);
}
