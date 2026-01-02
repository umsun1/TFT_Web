package com.tft.web.model.dto;

import lombok.Data;

@Data
public class SummonerProfileDto {
    private String summonerName;
    private String tagLine;
    private String puuid;
    
    private String tier;
    private String rank;
    private int lp;
    
    private int wins;
    private int losses;
    
    private int profileIconId;
    private long summonerLevel;

    private long winCount; // 1등 횟수
    private double avgPlacement;
    private double top4Rate;
    private double winRate;

    // [중앙 상단용: 최근 20게임 상세 통계] - 새로 추가할 것들
    private double recentAvgPlacement;
    private double recentTop4Rate;
    private double recentWinRate;
    private int[] rankCounts; // [1등횟수, 2등횟수, ..., 8등횟수] -> 차트용
}
