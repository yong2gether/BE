package com.yong2gether.ywave.preference.service;

import com.yong2gether.ywave.preference.domain.UserPreferenceRegion;
import com.yong2gether.ywave.preference.dto.RegionResponse;
import com.yong2gether.ywave.preference.dto.SetPreferredRegionRequest;
import com.yong2gether.ywave.preference.repository.RegionCenterRepository;
import com.yong2gether.ywave.preference.repository.UserPreferenceRegionRepository;
import com.yong2gether.ywave.user.domain.User;
import com.yong2gether.ywave.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PreferenceRegionService {

    private final UserPreferenceRegionRepository repository;
    private final UserRepository userRepository;
    private final RegionCenterReadService centerReadService;

    private static final String ONLY_SIDO = "경기도";
    private static final String DONG_ALL = "전체";

    // --- DONG 헬퍼 ---
    private boolean isAllDong(String v) {
        if (v == null) return true; // 구버전 클라 호환: null/빈문자 → 전체
        v = v.trim();
        return v.isEmpty() || DONG_ALL.equals(v);
    }
    private String normalizeDongForStorage(String v) {
        return isAllDong(v) ? DONG_ALL : v.trim();
    }
    private String normalizeDongForLookup(String v) {
        return isAllDong(v) ? null : v.trim(); // 리포 조회시 전체→null
    }

    @Transactional
    public RegionResponse setRegion(Long userId, SetPreferredRegionRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        String sido = n(req.getSido());
        String sigungu = n(req.getSigungu());
        String dongForStorage = normalizeDongForStorage(req.getDong());
        String dongForLookup  = normalizeDongForLookup(req.getDong());

        if (!ONLY_SIDO.equals(sido)) {
            throw new IllegalArgumentException("경기도 내 지역만 설정할 수 있습니다.");
        }
        if (sigungu == null) {
            throw new IllegalArgumentException("시/군/구는 필수입니다.");
        }

        var existingOpt = repository.findOneByUser_Id(userId);
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            if (Objects.equals(existing.getSido(), sido)
                    && Objects.equals(existing.getSigungu(), sigungu)
                    && Objects.equals(existing.getDong(), dongForStorage)) {
                // 좌표까지 이미 있으면 바로 반환
                return new RegionResponse(existing.getSido(), existing.getSigungu(),
                        existing.getDong(), existing.getLat(), existing.getLng());
            }
        }

        // 좌표 계산: dong 정확매칭 → 시군구 대표(=dong IS NULL 우선)
        var center = centerReadService.resolve(sido, sigungu, dongForLookup)
                .orElseThrow(() -> new IllegalArgumentException("해당 지역의 센터 좌표를 찾을 수 없습니다."));

        Double lat = center.getLat();
        Double lng = center.getLng();

        // 사용자당 1개만 유지: 기존 삭제 후 저장
        repository.deleteByUser_Id(userId);
        repository.save(UserPreferenceRegion.of(user, sido, sigungu, dongForStorage, lat, lng));

        return new RegionResponse(sido, sigungu, dongForStorage, lat, lng);
    }

    @Transactional(readOnly = true)
    public Optional<RegionResponse> getRegion(Long userId) {
        return repository.findOneByUser_Id(userId)
                .map(r -> {
                    Double lat = r.getLat();
                    Double lng = r.getLng();
                    if (lat == null || lng == null) {
                        // 저장 시 못채웠다면 조회 시 보정 (전체→null 로 조회)
                        String dongForLookup = normalizeDongForLookup(r.getDong());
                        var c = centerReadService.resolve(r.getSido(), r.getSigungu(), dongForLookup);
                        lat = c.map(RegionCenterRepository.LatLng::getLat).orElse(null);
                        lng = c.map(RegionCenterRepository.LatLng::getLng).orElse(null);
                    }
                    return new RegionResponse(r.getSido(), r.getSigungu(), r.getDong(), lat, lng);
                });
    }

    private String n(String v) {
        if (v == null) return null;
        v = v.trim();
        return v.isEmpty() ? null : v;
    }
}
