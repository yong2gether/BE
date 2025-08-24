package com.yong2gether.ywave.preference.repository;

import com.yong2gether.ywave.preference.domain.UserPreferenceRegion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPreferenceRegionRepository extends JpaRepository<UserPreferenceRegion, Long> {
    List<UserPreferenceRegion> findByUser_Id(Long userId);
    void deleteByUser_Id(Long userId);
    boolean existsByUser_Id(Long userId);

    Optional<UserPreferenceRegion> findOneByUser_Id(Long userId);
}
