package com.tft.web.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.tft.web.domain.Participant;
import com.tft.web.model.dto.MatchApiDto;
import com.tft.web.model.dto.ParticipantDto;
import com.tft.web.model.dto.SummonerProfileDto;
import com.tft.web.model.dto.UnitDto;
import com.tft.web.service.MatchService;
import com.tft.web.service.SummonerService;

@Controller
public class MainController {
	
	@Autowired
	SummonerService summonerService;

	@Autowired
	MatchService matchService;

	@GetMapping("/")
	public String main(Model model) {
        model.addAttribute("name", "TFT Player");
		return "tft/index";
	}

    @GetMapping("/summoner/{server}/{gameName}/{tagLine}")
	public String getSummoner(@PathVariable String server, @PathVariable String gameName, @PathVariable String tagLine, Model model){
		SummonerProfileDto profile = summonerService.getSummonerData(server, gameName, tagLine);
		
		String puuid = profile.getPuuid();
		
		// matchIds에서 내 데이터만 추출
		List<String> matchIds = matchService.getMatchIds(puuid);
		List<MatchApiDto> matches = matchService.getMatchDetail(matchIds, puuid);
		
		// // 서비스 호출
		// Page<MatchApiDto> matchPage = matchService.getRecentMatches(puuid, page);
		// List<MatchApiDto> matches = matchPage.getContent();
		// // 콘솔 출력 확인
		// System.out.println("=== 검증 시작 ===");
		// if (!matches.isEmpty()) {
		// 	// processDtoForView가 호출되었으므로 participants 리스트에는 '나' 1명만 들어있음
		// 	ParticipantDto my = matches.get(0).getInfo().getParticipants().get(0);
		// 	System.out.println("내 등수: " + my.getPlacement());
		// 	System.out.println("골드: " + my.getGold_left());
			
		// 	if (!my.getUnits().isEmpty()) {
		// 		System.out.println("첫 유닛 이름: " + my.getUnits().get(0).getChampionName());
		// 		System.out.println("첫 유닛 이미지: " + my.getUnits().get(0).getChampionImg());
		// 	}
		// }
		// System.out.println("=== 검증 종료 ===");
		

		model.addAttribute("matches", matches);
		model.addAttribute("profile", profile);
		// model.addAttribute("matches", matchContent);
		// model.addAttribute("hasNext", matchPage.hasNext());       // 다음 페이지 존재 여부 (true/false)
		// model.addAttribute("currentPage", matchPage.getNumber()); // 현재 페이지 번호 (0)
		// model.addAttribute("totalMatches", matchPage.getTotalElements()); // 전체 판수 (예: 190)

		return "tft/summoner";
	}
	
}