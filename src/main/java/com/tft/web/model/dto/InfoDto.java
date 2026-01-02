package com.tft.web.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InfoDto {
    private long game_datetime;
    private List<ParticipantDto> participants;
}