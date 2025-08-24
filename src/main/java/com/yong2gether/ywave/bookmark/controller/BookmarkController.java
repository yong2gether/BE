package com.yong2gether.ywave.bookmark.controller;

import com.yong2gether.ywave.bookmark.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Bookmarks", description = "가맹점 북마크 생성/취소 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores/{storeId}/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(
            summary = "가맹점 북마크 생성",
            description = "X-USER-ID의 사용자에 대해 해당 storeId를 북마크합니다. groupId가 없으면 기본 그룹을 사용합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = CreateRes.class))),
            @ApiResponse(responseCode = "409", description = "이미 북마크된 가맹점", content = @Content)
    })
    @PostMapping
    public ResponseEntity<CreateRes> create(
            @Parameter(name = "X-USER-ID", description = "사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "가맹점 ID", required = true)
            @PathVariable Long storeId,
            @RequestBody(required = false) CreateReq req
    ) {
        Long id = bookmarkService.create(userId, storeId, (req != null ? req.groupId : null));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateRes(id, storeId, "가맹점 북마크가 생성되었습니다."));
    }

    @Operation(
            summary = "가맹점 북마크 취소",
            description = "X-USER-ID의 사용자에 대해 해당 storeId의 북마크를 취소합니다. 성공 시 200과 메시지를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = DeleteRes.class))),
            @ApiResponse(responseCode = "404", description = "북마크 없음(팀 정책상 사용하는 경우)", content = @Content)
    })
    @DeleteMapping
    public ResponseEntity<DeleteRes> delete(
            @Parameter(name = "X-USER-ID", description = "사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "가맹점 ID", required = true)
            @PathVariable Long storeId
    ) {
        bookmarkService.deleteByUserAndStore(userId, storeId);
        return ResponseEntity.ok(new DeleteRes(storeId, "가맹점 북마크가 취소되었습니다."));
    }

    public record CreateReq(Long groupId) {}
    public record CreateRes(Long bookmarkId, Long storeId, String message) {}
    public record DeleteRes(Long storeId, String message) {}
}
