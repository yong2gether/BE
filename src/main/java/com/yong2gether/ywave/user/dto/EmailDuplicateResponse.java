package com.yong2gether.ywave.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailDuplicateResponse {
    private String message;
    private boolean duplicated;
}
