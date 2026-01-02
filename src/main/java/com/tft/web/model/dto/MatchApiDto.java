package com.tft.web.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchApiDto {
    private MetadataDto metadata;
    private InfoDto info;
}
