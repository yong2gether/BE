// src/main/java/com/yong2gether/ywave/mypage/service/BookmarkQueryService.java
package com.yong2gether.ywave.mypage.service;

import com.yong2gether.ywave.bookmark.domain.BookmarkGroup;
import com.yong2gether.ywave.bookmark.repository.BookmarkRepository;
import com.yong2gether.ywave.bookmark.repository.projection.BookmarkFlatView;
import com.yong2gether.ywave.mypage.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkQueryService {

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkGroupCommandService bookmarkGroupCommandService;

    @Transactional // 메서드 단위로 write 허용
    public List<BookmarkGroupDto> getBookmarkedGroups(Long userId) {
        // 1) 기본 그룹 보장하고, 그 엔티티를 받아둔다
        BookmarkGroup defaultGroup = bookmarkGroupCommandService.ensureDefaultGroup(userId);

        // 2) 모든 그룹(빈 그룹 포함) 조회
        List<BookmarkFlatView> rows = bookmarkRepository.findAllGroupsWithStores(userId);

        // 3) 혹시라도 0건이면(예: 잘못된 JOIN/신규 사용자) 기본 그룹만 내려준다
        if (rows.isEmpty()) {
            return List.of(new BookmarkGroupDto(
                    defaultGroup.getId(),
                    defaultGroup.getName(),
                    defaultGroup.isDefault(),
                    defaultGroup.getIconUrl(),
                    new ArrayList<>()
            ));
        }

        // 4) 결과 그룹핑
        Map<Long, BookmarkGroupDto> map = new LinkedHashMap<>();
        for (BookmarkFlatView r : rows) {
            BookmarkGroupDto groupDto = map.computeIfAbsent(
                    r.getGroupId(),
                    id -> new BookmarkGroupDto(
                            r.getGroupId(),
                            r.getGroupName(),
                            Boolean.TRUE.equals(r.getIsDefault()),
                            null, // iconUrl은 현재 projection에서 가져올 수 없음, 나중에 별도 조회 필요
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
        
        // 5) 각 그룹의 iconUrl을 별도로 조회하여 설정
        List<BookmarkGroupDto> result = new ArrayList<>();
        for (BookmarkGroupDto groupDto : map.values()) {
            // 각 그룹의 iconUrl을 조회
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
        // 1) 그룹 정보 조회
        BookmarkGroup group = bookmarkGroupCommandService.getGroupById(groupId);
        
        // 2) 사용자 권한 확인 (자신의 그룹만 조회 가능)
        if (!group.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 그룹에 대한 접근 권한이 없습니다.");
        }
        
        // 3) 해당 그룹의 가맹점 ID 목록 조회
        List<Long> storeIds = bookmarkRepository.findStoreIdsByGroupId(groupId);
        
        // 4) DTO 생성 및 반환
        return new BookmarkGroupDetailDto(
            group.getId(),
            group.getName(),
            group.getIconUrl(),
            group.isDefault(),
            storeIds
        );
    }
}
