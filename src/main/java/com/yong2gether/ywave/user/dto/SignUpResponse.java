package com.yong2gether.ywave.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class SignUpResponse {
    private Long id;

    private String email;
    private String nickname;
    private String photoUrl;
    private Boolean gpsAllowed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
