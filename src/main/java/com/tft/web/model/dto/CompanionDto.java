package com.tft.web.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanionDto {

    private String content_ID;
    private int item_ID;
    private int skin_ID;
    private String species;

    private String companionImg;
}