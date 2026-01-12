package com.tft.web.controller;

import com.tft.web.service.TftStaticDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final TftStaticDataService staticDataService;

    @GetMapping("/board")
    public String board(Model model) {
        model.addAttribute("champions", staticDataService.getAllChampions());
        
        // 시너지 정보를 위한 맵 추가 (ID -> 한글명)
        java.util.Map<String, String> traitNames = new java.util.HashMap<>();
        staticDataService.getAllChampions().stream()
            .flatMap(c -> c.getTraits().stream())
            .distinct()
            .forEach(t -> traitNames.put(t, staticDataService.getTraitKoName(t)));
        model.addAttribute("traitNames", traitNames);
        
        return "tft/board";
    }
}
