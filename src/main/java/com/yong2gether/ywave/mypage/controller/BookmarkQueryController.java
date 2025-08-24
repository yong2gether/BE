// src/main/java/com/yong2gether/ywave/mypage/controller/BookmarkQueryController.java
package com.yong2gether.ywave.mypage.controller;

import com.yong2gether.ywave.mypage.dto.*;
import com.yong2gether.ywave.mypage.service.BookmarkQueryService;
import com.yong2gether.ywave.mypage.service.BookmarkGroupCommandService;
import com.yong2gether.ywave.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class BookmarkQueryController {

    private final BookmarkQueryService bookmarkQueryService;
    private final BookmarkGroupCommandService bookmarkGroupCommandService;
    private final UserRepository userRepository;

    @Operation(summary="북마크한 가맹점 그룹별 조회",
            description="특정 사용자의 북마크한 가맹점을 그룹 단위로 조회합니다.(default: 기본그룹)")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="조회 성공"),
            @ApiResponse(responseCode="403", description="권한 없음")
    })
    @GetMapping("/bookmarks/groups")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookmarkedGroupsResponse> getBookmarkedGroups(
            Authentication authentication
    ) {
        String email = (authentication != null ? authentication.getName() : null);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 필요");
        }
        Long userId = userRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다."));

        System.out.println("[DEBUG] bookmarks principal=" + email + ", userId=" + userId);
        List<BookmarkGroupDto> groups = bookmarkQueryService.getBookmarkedGroups(userId);
        return ResponseEntity.ok(BookmarkedGroupsResponse.ok(groups));
    }

    @Operation(summary="북마크 그룹 생성",
            description="내부 인증된 사용자 기준으로 북마크 그룹을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="생성 성공"),
            @ApiResponse(responseCode="403", description="권한 없음")
    })
    @PostMapping("/bookmarks/groups")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CreateBookmarkGroupResponse> createBookmarkGroup(
            Authentication authentication,
            @RequestBody CreateBookmarkGroupRequest request
    ) {
        String email = (authentication != null ? authentication.getName() : null);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 필요");
        }
        Long userId = userRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다."));

        String groupName = request.groupName();
        if (groupName == null || groupName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "그룹 이름은 필수입니다.");
        }

        // 그룹 생성 (iconUrl은 현재 도메인에 저장 필드가 없어 응답에만 반영)
        com.yong2gether.ywave.bookmark.domain.BookmarkGroup saved =
                bookmarkGroupCommandService.createGroup(userId, groupName);

        CreatedBookmarkGroupDto group = new CreatedBookmarkGroupDto(
                saved.getId(),
                saved.getName(),
                request.iconUrl()
        );
        return ResponseEntity.ok(CreateBookmarkGroupResponse.ok(group));
    }

    @Operation(summary="북마크 그룹 삭제",
            description="내부 인증된 사용자 기준으로 북마크 그룹을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="삭제 성공"),
            @ApiResponse(responseCode="403", description="권한 없음"),
            @ApiResponse(responseCode="404", description="그룹 없음")
    })
    @DeleteMapping("/bookmarks/groups")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeleteBookmarkGroupResponse> deleteBookmarkGroup(
            Authentication authentication,
            @RequestBody DeleteBookmarkGroupRequest request
    ) {
        String email = (authentication != null ? authentication.getName() : null);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 필요");
        }
        Long userId = userRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다."));

        if (request.groupId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "groupId는 필수입니다.");
        }

        bookmarkGroupCommandService.deleteGroup(userId, request.groupId());
        return ResponseEntity.ok(DeleteBookmarkGroupResponse.ok(request.groupId()));
    }
}
