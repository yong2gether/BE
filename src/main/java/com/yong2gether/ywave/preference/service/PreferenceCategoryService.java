package com.yong2gether.ywave.preference.service;

import com.yong2gether.ywave.preference.domain.CategoryType;
import com.yong2gether.ywave.preference.domain.UserPreferenceCategory;
import com.yong2gether.ywave.preference.dto.MessageResponse;
import com.yong2gether.ywave.preference.repository.UserPreferenceCategoryRepository;
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

    @Transactional
    public MessageResponse setCategories(Long userId, List<String> categoryNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (categoryNames == null || categoryNames.isEmpty()) {
            throw new IllegalArgumentException("최소 1개 이상의 카테고리를 선택해주세요.");
        }
        if (categoryNames.size() > 10) {
            throw new IllegalArgumentException("카테고리는 최대 10개까지 선택 가능합니다.");
        }

        // 중복 제거 + Enum 변환
        Set<CategoryType> targets = categoryNames.stream()
                .filter(Objects::nonNull)
                .map(CategoryType::from)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(CategoryType.class)));

        // 기존 중에서 선택되지 않은 건 삭제
        repository.deleteByUser_IdAndCategoryNotIn(userId, targets);

        // 없는 것만 추가
        for (CategoryType ct : targets) {
            if (!repository.existsByUser_IdAndCategory(userId, ct)) {
                repository.save(UserPreferenceCategory.of(user, ct));
            }
        }

        return new MessageResponse("선호 카테고리가 성공적으로 저장되었습니다.");
    }

    @Transactional(readOnly = true)
    public List<String> getCategories(Long userId) {
        return repository.findByUser_Id(userId)
                .stream()
                .map(pc -> pc.getCategory().kor())
                .toList();
    }
}
