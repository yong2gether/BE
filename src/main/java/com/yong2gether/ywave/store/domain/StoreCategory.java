package com.yong2gether.ywave.store.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "store_category", schema = "core",
        indexes = { @Index(name = "ix_store_category_category_id", columnList = "category_id") })
public class StoreCategory {

    @Id
    @Column(name = "store_id")
    private Long storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;         // 기존 Category 엔티티

    @Column(name = "subcategory", length = 120)
    private String subcategory;

    protected StoreCategory() {}

    public StoreCategory(Long storeId, Category category, String subcategory) {
        this.storeId = storeId;
        this.category = category;
        this.subcategory = subcategory;
    }

    public Long getStoreId() { return storeId; }
    public Category getCategory() { return category; }
    public String getSubcategory() { return subcategory; }
    public void setCategory(Category category) { this.category = category; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
}
