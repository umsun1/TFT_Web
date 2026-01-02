package com.tft.web.model.dto;

import lombok.Data;

@Data
public class TftLeagueEntryDto {
    //TFT 플레이어 정보
    private String queueType;
    private String tier;
    private String rank;

    private int wins;
    private int losses;
    private int leaguePoints;
}
