package com.tft.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tft.web.domain.MetaDeck;
import com.tft.web.repository.MetaDeckRepository;
import com.tft.web.service.TftStaticDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MetaController {

    private final MetaDeckRepository metaDeckRepository;
    private final TftStaticDataService staticDataService;
    private final ObjectMapper objectMapper;

    @GetMapping("/meta")
    public String meta(Model model) {
        List<MetaDeck> decks = metaDeckRepository.findAllByOrderByAvgPlacementAsc();

        // 한글화 및 데이터 가공
        List<MetaDeckDto> dtos = decks.stream().map(d -> {
            MetaDeckDto dto = new MetaDeckDto();
            
            // 이름 파싱 (예: "7 Pentakill Karthus Viego" -> "7 펜타킬 카서스 비에고")
            String[] parts = d.getName().split(" ");
            if (parts.length >= 2) {
                String traitCount = parts[0]; // "7"
                String traitEng = parts[1]; // "Pentakill"
                String traitKo = staticDataService.getTraitKoName(traitEng);
                
                StringBuilder nameBuilder = new StringBuilder();
                nameBuilder.append(traitCount).append(" ").append(traitKo != null ? traitKo : traitEng);

                // 캐리 유닛들이 있는 경우
                for (int i = 2; i < parts.length; i++) {
                    String unitEng = parts[i];
                    String unitKo = staticDataService.getChampionKoName(unitEng);
                    nameBuilder.append(" ").append(unitKo != null ? unitKo : unitEng);
                }
                
                dto.setName(nameBuilder.toString());
                dto.setMainTraitIcon(staticDataService.getTraitIconUrl(traitEng));
            } else {
                dto.setName(d.getName());
            }

            dto.setAvgPlacement(d.getAvgPlacement());
            dto.setWinRate(d.getWinRate());
            dto.setTop4Rate(d.getTop4Rate());
            dto.setPickRate(d.getPickRate());
            dto.setTier(d.getTier());

            // 유닛 이미지 URL 매핑
            try {
                List<String> unitNames = objectMapper.readValue(d.getCoreUnits(), new TypeReference<List<String>>() {});
                dto.setCoreUnits(unitNames.stream()
                        .map(name -> new UnitDto(staticDataService.getChampionKoName(name), staticDataService.getChampionIconUrl(name), staticDataService.getChampionCost(name)))
                        .collect(Collectors.toList()));
            } catch (Exception e) {
                dto.setCoreUnits(List.of());
            }

            return dto;
        }).collect(Collectors.toList());

        model.addAttribute("decks", dtos);
        return "tft/meta";
    }

    @lombok.Data
    public static class MetaDeckDto {
        private String name;
        private String mainTraitIcon;
        private double avgPlacement;
        private double winRate;
        private double top4Rate;
        private double pickRate;
        private String tier;
        private List<UnitDto> coreUnits;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class UnitDto {
        private String name;
        private String imgUrl;
        private int cost;
    }
}
