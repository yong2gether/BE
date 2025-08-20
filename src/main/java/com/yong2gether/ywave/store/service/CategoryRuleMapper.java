package com.yong2gether.ywave.store.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

// service/CategoryRuleMapper.java
@Component
public class CategoryRuleMapper {
    private static final Pattern P_FOOD   = Pattern.compile("치킨|한식|분식|곱창|회|초밥|고기|식당|뷔페|덮밥|일식|막창|전골|중국집|중식|짜장|짬뽕|막국수|떡볶이|쌀국수|포메인|피자|파스타|스파게티|레스토랑");
    private static final Pattern P_CAFE   = Pattern.compile("카페|커피|tea|디저트|베이커리|빵집|스타벅스|투썸");
    private static final Pattern P_MOVIE  = Pattern.compile("영화관|시네마|씨네|공연장|극장");
    private static final Pattern P_MED    = Pattern.compile("병원|의원|치과|한의원|약국|약방|응급실|내과|소아과|물리치료");
    private static final Pattern P_MART   = Pattern.compile("마트|슈퍼|편의점|구멍가게|코스트코");
    private static final Pattern P_EDU    = Pattern.compile("학원|학교|문구|서점|독서실");
    private static final Pattern P_LODG   = Pattern.compile("호텔|모텔|게스트하우스|펜션|리조트");
    private static final Pattern P_LIFE   = Pattern.compile("세탁|이사|미용|네일|피부|수리|철물|인테리어|부동산");
    private static final Pattern P_APP    = Pattern.compile("의류|옷|잡화|신발|악세|가방|패션");
    private static final Pattern P_SPORTS = Pattern.compile("헬스|요가|필라테스|체육관|스포츠|골프|볼링");
    private static final Pattern P_GAS    = Pattern.compile("주유소|충전소|LPG|셀프주유");

    public String mapMajor(String name, String sector, String prod) {
        String t = (nz(name) + "|" + nz(sector) + "|" + nz(prod)).toLowerCase();
        if (P_FOOD.matcher(t).find()) return "FOOD";
        if (P_CAFE.matcher(t).find()) return "CAFE";
        if (P_MOVIE.matcher(t).find()) return "MOVIE_SHOW";
        if (P_MED.matcher(t).find()) return "MEDICAL";
        if (P_MART.matcher(t).find()) return "MART_SUPER";
        if (P_EDU.matcher(t).find()) return "EDUCATION_STATIONERY";
        if (P_LODG.matcher(t).find()) return "LODGING";
        if (P_LIFE.matcher(t).find()) return "LIVING_CONVENIENCE";
        if (P_APP.matcher(t).find()) return "APPAREL_MISC";
        if (P_SPORTS.matcher(t).find()) return "SPORTS";
        if (P_GAS.matcher(t).find()) return "GAS_STATION";
        return null; // 못 찾으면 null
    }
    private static String nz(String s){ return s==null?"":s; }
}

