package com.tft.web.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TraitDto {
    private String name;            // ex) TFT16_DarkChild
    private int tier_current;       // 현재 단계
    private int tier_total;         // 최대 단계
    private int num_units;          // 유닛 수
    private int style;              // 시너지 등급

    // 가공된 데이터 (View Data)
    private String iconUrl;
    private String bgUrl;
}