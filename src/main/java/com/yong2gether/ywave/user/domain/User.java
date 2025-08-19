package com.yong2gether.ywave.user.domain;

import com.yong2gether.ywave.global.domain.BaseTime;
import com.yong2gether.ywave.store.domain.Category;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id") // 무한 Lazy 로딩 폭탄을 방지하기 위한 어노테이션
@ToString(exclude = "password")
@Entity
@Table(name="users")
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 40)
    private String nickname;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Column(name = "is_gps_allowed", nullable = false)
    private Boolean gpsAllowed;


    // @CreatedDate, LastModifiedDate 어노테이션을 쓰면 엔티티가 저장 수정될때 시간 자동으로 채워줌!
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 선호 카테고리 조인 엔티티
    // @Builder가 아닌 Builder.Default로 함 -> 왜? 기본값이 필수인 속성들로 정해놔서...!
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PreferenceCategory> preferences = new HashSet<>();

    // @EqualsAndHashCode(of="id")에서 Lombok의 id가 둘다 null(ex. 아직 저장 안된 엔티티 두 개의 id)인 경우
    // equals == true가 될 수 있는 걸 미리 방지
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id != null && id.equals(other.id);
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // 편의 메서드
    public void addPreference(Category category) {
        PreferenceCategory link = PreferenceCategory.of(this, category);
        this.preferences.add(link);
    }

    public void removePreference(Category category) {
        this.preferences.removeIf(pc -> pc.getCategory().equals(category));
    }

    public void replacePreferences(Set<Category> categories) {
        this.preferences.clear();
        if (categories != null) {
            categories.forEach(this::addPreference);
        }
    }

    public void changePassword(String encodedPassword) { this.password = encodedPassword; }
    public void changeNickname(String nickname) { this.nickname = nickname; }
    public void changePhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void allowGps(boolean allowed) { this.gpsAllowed = allowed; }
}
