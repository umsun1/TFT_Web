package com.tft.web.model.dto;

import lombok.Data;

@Data
public class RiotAccountDto {
    //소환사 정보 조회의 기반
    private String gameName;
    private String tagLine;
    private String puuid;
}
