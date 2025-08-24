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
    public BookmarkGroup createGroup(Long userId, String groupName, String iconUrl) {
        if (groupRepo.existsByUserIdAndName(userId, groupName)) {
            throw new DuplicateGroupNameException("이미 존재하는 그룹 이름입니다.");
        }
        User user = userRepo.getReferenceById(userId);
        BookmarkGroup g = BookmarkGroup.builder()
                .user(user)
                .name(groupName)
                .iconUrl(iconUrl)
                .isDefault(false)
                .build();
        return groupRepo.save(g);
    }

    public static class DuplicateGroupNameException extends RuntimeException {
        public DuplicateGroupNameException(String message) { super(message); }
    }

    @Transactional
    public BookmarkGroup updateGroup(Long userId, Long groupId, String newGroupName, String newIconUrl) {
        BookmarkGroup group = groupRepo.findById(groupId)
                .orElseThrow(GroupNotFoundException::new);
        if (!group.getUser().getId().equals(userId)) {
            throw new NotOwnerOfGroupException();
        }
        if (group.isDefault()) {
            throw new CannotUpdateDefaultGroupException();
        }
        
        // 그룹 이름이 제공된 경우에만 중복 체크 및 수정
        if (newGroupName != null && !newGroupName.isBlank()) {
            if (groupRepo.existsByUserIdAndNameAndIdNot(userId, newGroupName, groupId)) {
                throw new DuplicateGroupNameException("이미 존재하는 그룹 이름입니다.");
            }
            group.setName(newGroupName);
        }
        
        // 아이콘 URL이 제공된 경우에만 수정
        if (newIconUrl != null) {
            group.setIconUrl(newIconUrl);
        }
        
        return groupRepo.save(group);
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
    public static class CannotUpdateDefaultGroupException extends RuntimeException {}
}
