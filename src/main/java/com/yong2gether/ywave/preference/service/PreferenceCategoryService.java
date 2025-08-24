package com.yong2gether.ywave.preference.service;

import com.yong2gether.ywave.preference.domain.UserPreferenceCategory;
import com.yong2gether.ywave.preference.dto.MessageResponse;
import com.yong2gether.ywave.preference.repository.UserPreferenceCategoryRepository;
import com.yong2gether.ywave.store.domain.Category;
import com.yong2gether.ywave.store.repository.CategoryRepository;
import com.yong2gether.ywave.user.domain.User;
import com.yong2gether.ywave.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreferenceCategoryService {

    private final UserPreferenceCategoryRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public MessageResponse setCategories(Long userId, List<Long> categoryIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new IllegalArgumentException("최소 1개 이상의 카테고리를 선택해주세요.");
        }
        // 중복 제거
        Set<Long> targets = categoryIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (targets.size() > 10) {
            throw new IllegalArgumentException("카테고리는 최대 10개까지 선택 가능합니다.");
        }

        // 유효한 카테고리인지 확인
        List<Category> categories = categoryRepository.findAllById(targets);
        if (categories.size() != targets.size()) {
            throw new IllegalArgumentException("유효하지 않은 카테고리 ID가 포함되어 있습니다.");
        }
        Map<Long, Category> categoryById = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // 기존 선호 목록
        List<UserPreferenceCategory> existing = repository.findByUser_Id(userId);
        Set<Long> existingIds = existing.stream()
                .map(upc -> upc.getCategory().getId())
                .collect(Collectors.toSet());

        // 삭제 대상 = 기존 - 신규
        List<UserPreferenceCategory> toDelete = existing.stream()
                .filter(upc -> !targets.contains(upc.getCategory().getId()))
                .toList();

        // 추가 대상 = 신규 - 기존
        List<UserPreferenceCategory> toAdd = targets.stream()
                .filter(id -> !existingIds.contains(id))
                .map(id -> UserPreferenceCategory.of(user, categoryById.get(id)))
                .toList();

        if (!toDelete.isEmpty()) repository.deleteAllInBatch(toDelete);
        if (!toAdd.isEmpty()) repository.saveAll(toAdd);

        return new MessageResponse("선호 카테고리가 성공적으로 저장되었습니다.");
    }

    @Transactional(readOnly = true)
    public List<String> getCategories(Long userId) {
        return repository.findByUser_Id(userId).stream()
                .map(upc -> upc.getCategory().getName()) // Category 마스터의 name 컬럼 기준
                .toList();
    }

}
