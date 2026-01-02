package com.tft.web.model.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetadataDto {
    private String match_id;
    private List<String> participants;
    private String data_version;
}