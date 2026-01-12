package com.tft.web.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChampionDto {
    private String id;
    private String name;
    private int cost;
    private String imgUrl;
    private List<String> traits;
}
