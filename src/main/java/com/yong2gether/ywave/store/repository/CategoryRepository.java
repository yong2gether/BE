package com.yong2gether.ywave.store.repository;

import com.yong2gether.ywave.store.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    List<Category> findByNameIn(List<String> names);

}
