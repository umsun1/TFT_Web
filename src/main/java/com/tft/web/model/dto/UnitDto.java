package com.tft.web.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UnitDto {
    @JsonProperty("character_id")
    private String characterId; // 예: "TFT13_Jayce"
    
    @JsonProperty("tier")
    private int star; // 1~3. FT에서 유닛의 성급(1성, 2성)은 'tier'라는 필드로 옵니다.
    
    @JsonProperty("rarity")
    private int cost; //1~5. 유닛의 가격/희귀도는 'rarity' 필드에 담깁니다.
    
    @JsonProperty("itemNames")
    private List<String> items;
    
    private String championName;
    private String championImg;
    private List<String> itemImgUrls;
}
