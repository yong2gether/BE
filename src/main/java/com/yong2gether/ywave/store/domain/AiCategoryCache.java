package com.yong2gether.ywave.store.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="ai_category_cache", schema="core")
public class AiCategoryCache {
    @Id
    @Column(length=300)
    private String signature;

    @Column(length=40, nullable=false)
    private String major;

    @Column(length=120)
    private String sub;

    @Column(name="sample_name", length=120)
    private String sampleName;

    // 기본 생성자
    public AiCategoryCache() {}

    public AiCategoryCache(String signature, String major, String sub, String sampleName) {
        this.signature = signature;
        this.major = major;
        this.sub = sub;
        this.sampleName = sampleName;
    }

    // getters/setters
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }
    public String getSampleName() { return sampleName; }
    public void setSampleName(String sampleName) { this.sampleName = sampleName; }
}
