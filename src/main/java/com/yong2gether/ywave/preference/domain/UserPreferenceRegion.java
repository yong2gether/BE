package com.yong2gether.ywave.preference.domain;

import com.yong2gether.ywave.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "preference_region", schema = "core")
public class UserPreferenceRegion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String sido;

    @Column(nullable = false)
    private String sigungu;

    @Column(nullable = false)
    private String dong;

    private Double lat;
    private Double lng;

    private UserPreferenceRegion(User user, String sido, String sigungu, String dong, Double lat, Double lng) {
        this.user = user;
        this.sido = sido;
        this.sigungu = sigungu;
        this.dong = dong;
        this.lat = lat;
        this.lng = lng;
    }

    public static UserPreferenceRegion of(User user, String sido, String sigungu, String dong, Double lat, Double lng) {
        return new UserPreferenceRegion(user, sido, sigungu, dong, lat, lng);
    }

    public void changeTo(String sido, String sigungu, String dong, Double lat, Double lng) {
        this.sido = sido;
        this.sigungu = sigungu;
        this.dong = dong;
        this.lat = lat;
        this.lng = lng;
    }
}
