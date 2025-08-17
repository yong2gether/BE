package com.yong2gether.ywave.store.domain;

import jakarta.persistence.*;
import lombok.*;

// store 테이블과 매핑
@Entity
@Table(name = "store")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Store {
    @Id
    @Column(length = 32)
    private String id; // pk, 가맹점 ID (예: s101)

    @Column(nullable = false, length = 100) // Column 세부 설정
    private String name;
}
