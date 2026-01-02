package com.tft.web.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.tft.web.domain.Participant;
import com.tft.web.model.dto.RiotAccountDto;
import com.tft.web.model.dto.SummonerDto;
import com.tft.web.model.dto.SummonerProfileDto;
import com.tft.web.model.dto.TftLeagueEntryDto;
import com.tft.web.repository.ParticipantRepository;

@Service
public  class SummonerServiceImp implements SummonerService{

    @Value("${riot.api.key}")
    private String apiKey;

    @Autowired
    private ParticipantRepository participantRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public SummonerProfileDto getSummonerData(String server, String gameName, String tagLine) {
        // 1. Riot ID → Account (서버 임시 고정)
        RiotAccountDto account = getAccountByRiotId(gameName, tagLine);
        if (account == null) return null;
        String puuid = account.getPuuid();

        // 2. PUUID → TFT League (배열로 오기 때문에 첫 번째 요소 추출)
        TftLeagueEntryDto league = getTftLeagueByPuuid(puuid);

        // 3. PUUID → Summoner (레벨, 아이콘 정보)
        SummonerDto summoner = getTftSummonerByPuuid(puuid);

        // 4. 데이터 조립 (Null 체크 포함)
        SummonerProfileDto profile = new SummonerProfileDto();
        profile.setSummonerName(account.getGameName());
        profile.setTagLine(account.getTagLine());
        profile.setPuuid(puuid);

        if (league != null) {
            Map<String, Object> stats = getWinStatistics(profile.getPuuid());
            double wins = league.getWins();
            double totalGames = league.getWins() + league.getLosses();
            double avgPlacement = getAveragePlacement(puuid);
            profile.setTier(league.getTier());
            profile.setRank(league.getRank());
            profile.setLp(league.getLeaguePoints());
            profile.setWins(league.getWins());
            profile.setLosses(league.getLosses());
            profile.setTop4Rate(wins/totalGames);
            profile.setAvgPlacement(avgPlacement);
            profile.setWinCount((long) stats.get("winCount"));
            profile.setWinRate((double) stats.get("winRate"));

        } else {
            // 리그 정보가 없는 경우 (언랭크)
            profile.setTier("UNRANKED");
            profile.setRank("");
        }
        if (summoner != null) {
            profile.setProfileIconId(summoner.getProfileIconId());
            profile.setSummonerLevel(summoner.getSummonerLevel());
        }
        return profile;
    }

    public RiotAccountDto getAccountByRiotId(String gameName, String tagLine) {
        String url = "https://asia.api.riotgames.com/riot/account/v1/accounts/by-riot-id/"+ gameName + "/" + tagLine;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", apiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<RiotAccountDto> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        RiotAccountDto.class
                );
                System.out.println(response.getBody());
        return response.getBody();
    }

    public TftLeagueEntryDto getTftLeagueByPuuid(String puuid) {
        String url = "https://kr.api.riotgames.com/tft/league/v1/by-puuid/" + puuid;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", apiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // List<TftLeagueEntryDto> 형태로 받아야 합니다.
        ResponseEntity<List<TftLeagueEntryDto>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        new org.springframework.core.ParameterizedTypeReference<List<TftLeagueEntryDto>>() {}
                );

        List<TftLeagueEntryDto> results = response.getBody();
        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        System.out.println(results);
        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        // 배열 중 첫 번째 요소(보통 랭크 정보)를 꺼내서 리턴
        return (results != null && !results.isEmpty()) ? results.get(0) : null;
    }

    public SummonerDto getTftSummonerByPuuid(String puuid){
        String url = "https://kr.api.riotgames.com/tft/summoner/v1/summoners/by-puuid/" + puuid;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", apiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<SummonerDto> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        SummonerDto.class
                );
                System.out.println(response.getBody());
        return response.getBody();
    }

    public double getAveragePlacement(String puuid) {
        // 1. DB에서 이 유저의 시즌 16 참가 기록만 가져옴
        List<Participant> seasonMatches = participantRepository.findByPaPuuidAndGameInfo_GaDatetimeAfter(puuid, LocalDateTime.of(2025, 12, 3, 0, 0)); // 시즌 시작일 기준)

        if (seasonMatches.isEmpty()) return 0.0;

        // 2. 등수 합산 및 평균 계산
        int totalRank = seasonMatches.stream()
                .mapToInt(Participant::getPaPlacement)
                .sum();

        return (double) totalRank / seasonMatches.size();
    }

    public Map<String, Object> getWinStatistics(String puuid) {
        // 1. 시즌 전체 매치 기록 조회
        List<Participant> seasonMatches = participantRepository.findByPaPuuid(puuid); // 시즌 필터 포함된 쿼리 권장

        if (seasonMatches.isEmpty()) {
            return Map.of("winCount", 0L, "winRate", 0.0);
        }

        // 2. 1등 횟수 계산
        long winCount = seasonMatches.stream()
                .filter(p -> p.getPaPlacement() == 1)
                .count();

        // 3. 승률 계산 (1등 횟수 / 전체 판수 * 100)
        double winRate = (double) winCount / seasonMatches.size() * 100.0;

        return Map.of("winCount", winCount, "winRate", winRate);
    }

    
    
}