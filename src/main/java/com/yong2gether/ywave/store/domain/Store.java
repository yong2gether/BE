package com.yong2gether.ywave.store.domain;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "stores", schema = "core")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String roadAddr;
    private String lotnoAddr;
    private String sido;
    private String sigungu;
    private String phone;
    private String sectorRaw;
    private String mainPrdRaw;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point geom;

    public Store() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRoadAddr() { return roadAddr; }
    public void setRoadAddr(String roadAddr) { this.roadAddr = roadAddr; }
    public String getLotnoAddr() { return lotnoAddr; }
    public void setLotnoAddr(String lotnoAddr) { this.lotnoAddr = lotnoAddr; }
    public String getSido() { return sido; }
    public void setSido(String sido) { this.sido = sido; }
    public String getSigungu() { return sigungu; }
    public void setSigungu(String sigungu) { this.sigungu = sigungu; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSectorRaw() { return sectorRaw; }
    public void setSectorRaw(String sectorRaw) { this.sectorRaw = sectorRaw; }
    public String getMainPrdRaw() { return mainPrdRaw; }
    public void setMainPrdRaw(String mainPrdRaw) { this.mainPrdRaw = mainPrdRaw; }
    public Point getGeom() { return geom; }
    public void setGeom(Point geom) { this.geom = geom; }
}
