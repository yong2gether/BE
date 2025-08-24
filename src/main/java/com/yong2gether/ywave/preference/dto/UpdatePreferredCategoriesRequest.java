package com.yong2gether.ywave.preference.dto;

import java.util.List;

public class UpdatePreferredCategoriesRequest {
    private List<Long> categoryIds;
    public List<Long> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<Long> categoryIds) { this.categoryIds = categoryIds; }
}
