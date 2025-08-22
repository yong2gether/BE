// src/main/java/com/yong2gether/ywave/mypage/controller/BookmarkQueryController.java
package com.yong2gether.ywave.mypage.controller;

import com.yong2gether.ywave.mypage.dto.*;
import com.yong2gether.ywave.mypage.service.BookmarkQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class BookmarkQueryController {

    private final BookmarkQueryService bookmarkQueryService;

    @Operation(summary="북마크한 가맹점 그룹별 조회",
            description="특정 사용자의 북마크한 가맹점을 그룹 단위로 조회합니다.(default: 기본그룹)")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="조회 성공"),
            @ApiResponse(responseCode="403", description="권한 없음")
    })
    @GetMapping("/{userId}/bookmarks/groups")
    @PreAuthorize("@authz.isSelfOrAdmin(#userId, authentication)")
    public ResponseEntity<BookmarkedGroupsResponse> getBookmarkedGroups(
            @PathVariable Long userId
    ) {
        List<BookmarkGroupDto> groups = bookmarkQueryService.getBookmarkedGroups(userId);
        return ResponseEntity.ok(BookmarkedGroupsResponse.ok(groups));
    }
}
