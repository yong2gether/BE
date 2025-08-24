package com.yong2gether.ywave.bookmark.domain;

import com.yong2gether.ywave.global.domain.BaseTime;
import com.yong2gether.ywave.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bookmark_group", schema = "core")
public class BookmarkGroup extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
    
    @Column(name = "icon_url", length = 500)
    private String iconUrl;
}


