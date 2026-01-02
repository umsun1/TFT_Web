package com.tft.web.model.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParticipantDto {

    // 식별
    private String puuid;
    private String riotIdGameName;
    private String riotIdTagline;
    
    // 결과
    private CompanionDto companion;
    private int placement;          // 최종 등수
    private int level;              // 최종 레벨
    private int gold_left;
    private int total_damage_to_players;

    // 덱 정보
    private List<UnitDto> units;
    private List<TraitDto> traits;
}