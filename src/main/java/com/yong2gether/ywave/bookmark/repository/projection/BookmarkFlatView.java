package com.yong2gether.ywave.bookmark.repository.projection;

public interface BookmarkFlatView {
    Long getGroupId();
    String getGroupName();
    Boolean getIsDefault();

    Long getStoreId();
    String getStoreName();

    // 아래 필드들은 현재 Store 엔티티와 다를 수 있으므로
    // 쿼리에서 null이 되더라도 컴파일은 가능하도록 유지합니다.
    String getCategory();
    String getRoadAddress();
    Double getLat();
    Double getLng();
    String getPhone();
    Double getRating();
    Integer getReviewCount();
    String getThumbnailUrl();
}


