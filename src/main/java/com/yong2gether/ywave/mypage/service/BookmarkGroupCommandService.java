// src/main/java/com/yong2gether/ywave/bookmark/service/BookmarkGroupCommandService.java
package com.yong2gether.ywave.mypage.service;

import com.yong2gether.ywave.bookmark.domain.BookmarkGroup;
import com.yong2gether.ywave.bookmark.repository.BookmarkGroupRepository;
import com.yong2gether.ywave.bookmark.repository.BookmarkRepository;
import com.yong2gether.ywave.user.domain.User;
import com.yong2gether.ywave.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class BookmarkGroupCommandService {

    private final BookmarkGroupRepository groupRepo;
    private final BookmarkRepository bookmarkRepo;
    private final UserRepository userRepo;

    @Transactional
    public BookmarkGroup ensureDefaultGroup(Long userId) {
        return groupRepo.findByUserIdAndIsDefaultTrue(userId)
                .orElseGet(() -> {
                    User user = userRepo.getReferenceById(userId);
                    BookmarkGroup g = BookmarkGroup.builder()
                            .user(user)
                            .name("기본 그룹")
                            .isDefault(true)
                            .build();
                    return groupRepo.save(g);
                });
    }

    @Transactional
    public BookmarkGroup createGroup(Long userId, String groupName) {
        if (groupRepo.existsByUserIdAndName(userId, groupName)) {
            throw new DuplicateGroupNameException("이미 존재하는 그룹 이름입니다.");
        }
        User user = userRepo.getReferenceById(userId);
        BookmarkGroup g = BookmarkGroup.builder()
                .user(user)
                .name(groupName)
                .isDefault(false)
                .build();
        return groupRepo.save(g);
    }

    public static class DuplicateGroupNameException extends RuntimeException {
        public DuplicateGroupNameException(String message) { super(message); }
    }

    @Transactional
    public void deleteGroup(Long userId, Long groupId) {
        BookmarkGroup group = groupRepo.findById(groupId)
                .orElseThrow(GroupNotFoundException::new);
        if (!group.getUser().getId().equals(userId)) {
            throw new NotOwnerOfGroupException();
        }
        if (group.isDefault()) {
            throw new CannotDeleteDefaultGroupException();
        }
        bookmarkRepo.deleteByGroup_Id(groupId);
        groupRepo.delete(group);
    }

    public static class GroupNotFoundException extends RuntimeException {}
    public static class NotOwnerOfGroupException extends RuntimeException {}
    public static class CannotDeleteDefaultGroupException extends RuntimeException {}
}
