package com.yong2gether.ywave.store.domain;

import com.yong2gether.ywave.user.domain.PreferenceCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "category",
        indexes = { @Index(name = "ix_category_name", columnList = "name") })
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60, unique = true)
    private String name;

    /** 역방향 필요 시만 유지 (API 단순화 원하면 생략 가능) */
    @OneToMany(mappedBy = "category")
    @Builder.Default
    private Set<PreferenceCategory> userLinks = new HashSet<>();
}

