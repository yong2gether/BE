package com.yong2gether.ywave.store.repository;

import com.yong2gether.ywave.store.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    List<Category> findByNameIn(Collection<String> names);
    @Query(value = "select * from core.category where id in (:ids)", nativeQuery = true)
    List<Category> findAllByIdInCore(@Param("ids") Collection<Long> ids);
}
