package com.yong2gether.ywave.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreController {
    private final JdbcTemplate jdbc;

    @GetMapping("/stores/nearby")
    public List<Map<String, Object>> nearby(
            @RequestParam double lng,
            @RequestParam double lat,
            @RequestParam(defaultValue = "1000") int radius,
            @RequestParam(defaultValue = "50") int limit) {

        return jdbc.queryForList("""
      SELECT id, name, sigungu,
             ST_X(geom)::float8 AS lng,
             ST_Y(geom)::float8 AS lat,
             ST_Distance(geog, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography)::int AS dist_m
      FROM core.stores
      WHERE geog IS NOT NULL
        AND ST_DWithin(geog, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, ?)
      ORDER BY dist_m
      LIMIT ?
      """, lng, lat, lng, lat, radius, limit);
    }
}
