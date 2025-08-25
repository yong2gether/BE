// src/main/java/com/yong2gether/ywave/mypage/service/BookmarkQueryService.java
package com.yong2gether.ywave.mypage.service;

import com.yong2gether.ywave.bookmark.domain.BookmarkGroup;
import com.yong2gether.ywave.bookmark.repository.BookmarkRepository;
import com.yong2gether.ywave.bookmark.repository.projection.BookmarkFlatView;
import com.yong2gether.ywave.mypage.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkQueryService {

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkGroupCommandService bookmarkGroupCommandService;

    @Transactional
    public List<BookmarkGroupDto> getBookmarkedGroups(Long userId) {

        BookmarkGroup defaultGroup = bookmarkGroupCommandService.ensureDefaultGroup(userId);

        List<BookmarkFlatView> rows = bookmarkRepository.findAllGroupsWithStores(userId);

        if (rows.isEmpty()) {
            return List.of(new BookmarkGroupDto(
                    defaultGroup.getId(),
                    defaultGroup.getName(),
                    defaultGroup.isDefault(),
                    defaultGroup.getIconUrl(),
                    new ArrayList<>()
            ));
        }

        Map<Long, BookmarkGroupDto> map = new LinkedHashMap<>();
        for (BookmarkFlatView r : rows) {
            BookmarkGroupDto groupDto = map.computeIfAbsent(
                    r.getGroupId(),
                    id -> new BookmarkGroupDto(
                            r.getGroupId(),
                            r.getGroupName(),
                            Boolean.TRUE.equals(r.getIsDefault()),
                            null,
                            new ArrayList<>()
                    )
            );
            if (r.getStoreId() != null) {
                groupDto.stores().add(new BookmarkedStoreDto(
                        r.getStoreId(), r.getStoreName(), r.getCategory(), r.getRoadAddress(),
                        r.getLat(), r.getLng(), r.getPhone(), r.getRating(), r.getReviewCount(), r.getThumbnailUrl()
                ));
            }
        }

        List<BookmarkGroupDto> result = new ArrayList<>();
        for (BookmarkGroupDto groupDto : map.values()) {
            String iconUrl = bookmarkRepository.findIconUrlByGroupId(groupDto.groupId());
            BookmarkGroupDto updatedGroupDto = new BookmarkGroupDto(
                    groupDto.groupId(),
                    groupDto.groupName(),
                    groupDto.isDefault(),
                    iconUrl,
                    groupDto.stores()
            );
            result.add(updatedGroupDto);
        }

        return result;
    }

    public BookmarkGroupDetailDto getBookmarkGroupDetail(Long userId, Long groupId) {
        BookmarkGroup group = bookmarkGroupCommandService.getGroupById(groupId);

        if (!group.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 그룹에 대한 접근 권한이 없습니다.");
        }

        List<Long> storeIds = bookmarkRepository.findStoreIdsByGroupId(groupId);

        return new BookmarkGroupDetailDto(
                group.getId(),
                group.getName(),
                group.getIconUrl(),
                group.isDefault(),
                storeIds
        );
    }


    public List<UserBookmarkItem> getUserBookmarks(Long userId) {
        return bookmarkRepository.findUserBookmarks(userId).stream()
                .map(v -> new UserBookmarkItem(
                        v.getBookmarkId(),
                        v.getStoreId(),
                        v.getBookmarkGroupId(),
                        v.getCreatedAt()
                ))
                .toList();
    }


    public List<Long> getBookmarkedStoreIdsIn(Long userId, Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) return List.of();
        return bookmarkRepository.findBookmarkedStoreIdsIn(userId, storeIds);
    }

    public Map<Long, Long> getBookmarkedInfosIn(Long userId, Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) return Map.of();
        return bookmarkRepository.findBookmarkedInfosIn(userId, storeIds).stream()
                .collect(Collectors.toMap(
                        BookmarkRepository.BookmarkedInfo::getStoreId,
                        BookmarkRepository.BookmarkedInfo::getBookmarkGroupId
                ));
    }


    public record UserBookmarkItem(Long bookmarkId,
                                   Long storeId,
                                   Long bookmarkGroupId,
                                   LocalDateTime createdAt) {}
}
