package com.tft.web.model.dto;

import lombok.Data;

@Data
public class SummonerDto {
    //소환사 계정 정보
    private int profileIconId;
    private long summonerLevel;
    private long revisionDate;
}
