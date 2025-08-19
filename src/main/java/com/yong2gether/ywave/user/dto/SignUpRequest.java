package com.yong2gether.ywave.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {
    @NotBlank @Email // 문자열 입력값
    private String email;

    @NotBlank @Size(min=8)
    private String password;


    @NotBlank @Size(max=40)
    private String nickname;

    private String photoUrl;

    @NotNull // 비문자 입력값
    private Boolean gpsAllowed;
}
