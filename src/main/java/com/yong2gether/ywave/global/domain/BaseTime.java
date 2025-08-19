package com.yong2gether.ywave.global.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
public abstract class BaseTime {
    @org.springframework.data.annotation.CreatedDate
    @Column(name="created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @org.springframework.data.annotation.LastModifiedDate
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
