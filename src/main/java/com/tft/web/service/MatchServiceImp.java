package com.tft.web.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tft.web.domain.GameInfo;
import com.tft.web.domain.Item;
import com.tft.web.domain.Participant;
import com.tft.web.domain.Trait;
import com.tft.web.domain.Unit;
import com.tft.web.model.dto.CompanionDto;
import com.tft.web.model.dto.InfoDto;
import com.tft.web.model.dto.MatchApiDto;
import com.tft.web.model.dto.MetadataDto;
import com.tft.web.model.dto.ParticipantDto;
import com.tft.web.model.dto.UnitDto;
import com.tft.web.repository.GameInfoRepository;
import com.tft.web.repository.ParticipantRepository;

import jakarta.transaction.Transactional;

import com.tft.web.model.dto.TraitDto;

@Service
public class MatchServiceImp implements MatchService {

    @Value("${riot.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TftStaticDataService tftStaticDataService;
    @Autowired
    private GameInfoRepository gameInfoRepository;
    @Autowired
    private ParticipantRepository participantRepository;

    @Override
    public List<String> getMatchIds(String puuid) {
        List<String> allMatchIds = new ArrayList<>();
        int start = 0;
        int count = 100; // 한 번에 가져올 최대치
        long endTime = 1764687600; // 2025년 12월 3일 기준

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", apiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            while (true) {
                // start 파라미터를 계속 증가시켜서 다음 페이지를 요청함
                String url = "https://asia.api.riotgames.com/tft/match/v1/matches/by-puuid/" + puuid 
                        + "/ids?startTime=" + endTime 
                        + "&start=" + start 
                        + "&count=" + count;

                ResponseEntity<List<String>> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity,
                        new org.springframework.core.ParameterizedTypeReference<List<String>>() {}
                );

                List<String> batch = response.getBody();

                // 1. 결과가 없거나 빈 리스트면 더 이상 가져올 게 없으므로 종료
                if (batch == null || batch.isEmpty()) {
                    break;
                }

                allMatchIds.addAll(batch);

                // 2. 만약 가져온 개수가 count(100)보다 적다면, 그게 마지막 페이지라는 뜻이므로 종료
                if (batch.size() < count) {
                    break;
                }

                // 3. 다음 100개를 가져오기 위해 시작 지점(start)을 100만큼 뒤로 미룸
                start += count;
            }
            
            System.out.println("수집된 총 매치 ID 개수: " + allMatchIds.size());
            return allMatchIds;

        } catch (Exception e) {
            System.err.println("매치 ID 전수 조사 실패: " + e.getMessage());
            return allMatchIds; // 에러 발생 전까지 수집된 데이터라도 반환
        }
    }

    @Override
    public List<MatchApiDto> getMatchDetail(List<String> matchIds, String myPuuid) {
        List<MatchApiDto> result = new ArrayList<>();
        for (String matchId : matchIds) {
            MatchApiDto dto = getSingleMatchDetail(matchId, myPuuid);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    private MatchApiDto getSingleMatchDetail(String matchId, String myPuuid) {
        // DB에서 먼저 매치 정보를 찾아봄.
        Optional<GameInfo> existingGame = gameInfoRepository.findByGaId(matchId);

        if (existingGame.isPresent()) {
            // DB에 데이터가 있다면 엔티티를 DTO로 변환해서 즉시 반환 (API 호출 안함)
            return convertEntityToDto(existingGame.get(), myPuuid);
        }
        // 없다면 API 호출.
        String url = "https://asia.api.riotgames.com/tft/match/v1/matches/" + matchId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", apiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) return null;

            MetadataDto metadata = objectMapper.convertValue(body.get("metadata"), MetadataDto.class);
            InfoDto info = objectMapper.convertValue(body.get("info"), InfoDto.class);

            // 이미 DB에 있는 매치인지 체크하고 저장하는 로직을 호출.
            saveMatchToDatabase(metadata, info);
            
            return processDtoForView(metadata, info, myPuuid);

        } catch (Exception e) {
            System.err.println("매치 상세 조회 실패: " + matchId);
            e.printStackTrace();
            return null;
        }
    }

    private MatchApiDto convertEntityToDto(GameInfo game, String myPuuid) {
        // 1. 기초 DTO 구조 생성
        MetadataDto metadata = new MetadataDto();
        metadata.setMatch_id(game.getGaId());

        InfoDto info = new InfoDto();
        List<ParticipantDto> pDtoList = new ArrayList<>();

        // 2. DB 엔티티를 DTO 리스트로 복원 (모든 참가자를 복원하거나 검색한 유저 것만 복원)
        for (Participant pa : game.getParticipants()) {
            ParticipantDto pDto = new ParticipantDto();
            pDto.setPuuid(pa.getPaPuuid());
            pDto.setPlacement(pa.getPaPlacement());
            pDto.setLevel(pa.getPaLevel());
            pDto.setGold_left(pa.getPaGold());
            pDto.setRiotIdGameName(pa.getPaName());
            pDto.setRiotIdTagline(pa.getPaTag());
            
            // 전설이
            if (pa.getPaCompanionId() != null) {
                CompanionDto cDto = new CompanionDto();
                cDto.setItem_ID(pa.getPaCompanionId());
                pDto.setCompanion(cDto);
            }

            // 시너지 복원
            pDto.setTraits(pa.getTraits().stream().map(t -> {
                TraitDto tDto = new TraitDto();
                tDto.setName(t.getTrName());
                tDto.setNum_units(t.getTrNumUnits());
                tDto.setStyle(t.getTrStyle());
                tDto.setTier_current(1); // DB에 저장되었다는 건 활성화되었다는 뜻
                return tDto;
            }).collect(Collectors.toList()));

            // 유닛 복원
            pDto.setUnits(pa.getUnits().stream().map(u -> {
                UnitDto uDto = new UnitDto();
                uDto.setCharacterId(u.getUnId());
                uDto.setStar(u.getUnTier());
                uDto.setCost(u.getUnCost());
                
                // 아이템 복원
                if (u.getItem() != null) {
                    List<String> items = new ArrayList<>();
                    if (u.getItem().getItFirst() != null) items.add(u.getItem().getItFirst());
                    if (u.getItem().getItSecond() != null) items.add(u.getItem().getItSecond());
                    if (u.getItem().getItThird() != null) items.add(u.getItem().getItThird());
                    uDto.setItems(items);
                }
                return uDto;
            }).collect(Collectors.toList()));

            pDtoList.add(pDto);
        }
        info.setParticipants(pDtoList);
        long epochMillis = game.getGaDatetime()
            .atZone(ZoneId.of("Asia/Seoul"))
            .toInstant()
            .toEpochMilli();

        info.setGame_datetime(epochMillis);
        // info.setGame_datetime(game.getGaDatetime());

        // 3. 핵심: 이미 만들어진 DTO 덩어리를 processDtoForView에 넘겨서 
        // 이미지 매핑과 내 정보 필터링을 한 번에 처리!
        return processDtoForView(metadata, info, myPuuid);
    }

    private MatchApiDto processDtoForView(MetadataDto metadata, InfoDto info, String myPuuid) {
        ParticipantDto myParticipant = null;
        for (ParticipantDto p : info.getParticipants()) {
            if (myPuuid.equals(p.getPuuid())) {
                myParticipant = p;
                
                // 1. 전설이 이미지 매핑
                if (myParticipant.getCompanion() != null) {
                    int itemId = myParticipant.getCompanion().getItem_ID();
                    String imgUrl = tftStaticDataService.getTacticianImgUrl(itemId);
                    myParticipant.getCompanion().setCompanionImg(imgUrl);
                }

                // 2. 유닛(챔피언) 및 아이템 이미지 매핑 (필터링 포함)
                if (myParticipant.getUnits() != null) {
                    List<UnitDto> filteredUnits = new ArrayList<>();
                    for (UnitDto unit : myParticipant.getUnits()) {
                        // 아타칸 제외 로직
                        if (unit.getCharacterId().toLowerCase().contains("atakhan")) continue;

                        unit.setChampionImg(tftStaticDataService.getUnitImgUrl(unit.getCharacterId()));
                        
                        if (unit.getItems() != null && !unit.getItems().isEmpty()) {
                            List<String> itemUrls = new ArrayList<>();
                            List<String> itemKoNames = new ArrayList<>();
                            for (String itemName : unit.getItems()) {
                                itemUrls.add(tftStaticDataService.getItemImgUrlByName(itemName));
                                String koItemName = tftStaticDataService.getItemKoName(itemName);
                                itemKoNames.add(koItemName);
                            }
                            unit.setItemImgUrls(itemUrls);
                            unit.setItems(itemKoNames);
                        }
                        String koName = tftStaticDataService.getUnitKoName(unit.getCharacterId());
                        unit.setChampionName(koName);
                        filteredUnits.add(unit);
                    }
                    myParticipant.setUnits(filteredUnits);
                }

                // 3. 시너지 가공 및 정렬 로직 추가
                if (myParticipant.getTraits() != null) {
                    Map<Integer, Integer> priorityMap = Map.of(3, 1, 5, 2, 4, 3, 2, 4, 1, 5);

                    List<TraitDto> sortedTraits = myParticipant.getTraits().stream()
                        .filter(t -> t.getTier_current() > 0)
                        .peek(t -> {
                            // 1. 아이콘 URL 추출 (이때 t.getName()은 아직 "TFT16_Glutton" 임)
                            String iconUrl = tftStaticDataService.getTraitIconUrl(t.getName());
                            t.setIconUrl(iconUrl);
                            
                            // 2. 배경 URL 설정
                            t.setBgUrl(getSynergyBgUrl(t.getStyle()));
                            
                            // 3. 마지막에 이름을 한글로 교체 (이제부터 t.getName()은 "대식가")
                            String koName = tftStaticDataService.getTraitKoName(t.getName());
                            t.setName(koName);
                        })
                        .sorted(Comparator.comparingInt(t -> priorityMap.getOrDefault(t.getStyle(), 99)))
                        .collect(Collectors.toList());

                    myParticipant.setTraits(sortedTraits);
                }
                break;
            }
        }

        if (myParticipant == null) return null;

        info.setParticipants(List.of(myParticipant));
        MatchApiDto match = new MatchApiDto();
        match.setMetadata(metadata);
        match.setInfo(info);
        return match;
    }

    // 시너지 배경 URL을 결정하는 헬퍼 메서드
    private String getSynergyBgUrl(int style) {
        return switch (style) {
            case 1 -> "https://cdn.dak.gg/tft/images2/tft/traits/background/bronze.svg";
            case 2 -> "https://cdn.dak.gg/tft/images2/tft/traits/background/silver.svg";
            case 3 -> "https://cdn.dak.gg/tft/images2/tft/traits/background/unique.svg";
            case 4 -> "https://cdn.dak.gg/tft/images2/tft/traits/background/gold.svg";
            case 5 -> "https://cdn.dak.gg/tft/images2/tft/traits/background/chromatic.svg";
            default -> "https://cdn.dak.gg/tft/images2/tft/traits/background/bronze.svg";
        };
    }

    @Override
    public Page<MatchApiDto> getRecentMatches(String puuid, int page) {
        // 1. 우선 DB 조회를 시도
        List<Participant> allParticipants = participantRepository.findByPaPuuid(puuid);

        // 2. [추가] DB에 데이터가 한 건도 없다면? 외부 API에서 가져오기
        if (allParticipants == null || allParticipants.isEmpty()) {
            System.out.println("DB에 데이터가 없습니다. API 호출을 시작합니다...");
            
            // Match ID 리스트 가져오기 (전수 조사)
            List<String> matchIds = getMatchIds(puuid);
            
            if (!matchIds.isEmpty()) {
                // 상세 정보 호출 및 DB 저장 (getMatchDetail 내부에서 saveMatchToDatabase 호출됨)
                // 최근 20개만 먼저 수집하거나, 전체를 수집하거나 선택 가능
                getMatchDetail(matchIds, puuid); 
                
                // 저장이 완료되었으므로 다시 DB에서 불러옴
                allParticipants = participantRepository.findByPaPuuid(puuid);
            }
        }

        // 3. 최신순 정렬
        allParticipants.sort((p1, p2) -> 
            p2.getGameInfo().getGaDatetime().compareTo(p1.getGameInfo().getGaDatetime()));

        // 4. 페이징 처리 (이하 동일)
        int pageSize = 20;
        int start = page * pageSize;
        int end = Math.min((start + pageSize), allParticipants.size());

        if (start >= allParticipants.size()) {
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, pageSize), allParticipants.size());
        }

        List<MatchApiDto> pagedDtos = allParticipants.subList(start, end).stream()
                .map(p -> convertEntityToDto(p.getGameInfo(), puuid)) 
                .collect(Collectors.toList());

        return new PageImpl<>(pagedDtos, PageRequest.of(page, pageSize), allParticipants.size());
    }

    @Transactional
    public void saveMatchToDatabase(MetadataDto metadata, InfoDto info) {
        // 1. 중복 체크 (DTO 필드명이 match_id이므로 getMatch_id() 호출)
        if (gameInfoRepository.existsByGaId(metadata.getMatch_id())) {
            return; 
        }

        // 2. GameInfo 엔티티 생성
        GameInfo game = new GameInfo();
        game.setGaId(metadata.getMatch_id());
        long timestampMs = info.getGame_datetime();

        LocalDateTime gameDateTime = Instant
                .ofEpochMilli(timestampMs)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime();

        game.setGaDatetime(gameDateTime);

        game.setGaVersion(metadata.getData_version());

        // 3. 참가자들(Participants) 순회
        for (ParticipantDto pDto : info.getParticipants()) {
            Participant p = new Participant();
            p.setPaPuuid(pDto.getPuuid());
            p.setPaPlacement(pDto.getPlacement());
            p.setPaLevel(pDto.getLevel());
            p.setPaGold(pDto.getGold_left());
            p.setPaName(pDto.getRiotIdGameName());
            p.setPaTag(pDto.getRiotIdTagline());

            if (pDto.getCompanion() != null) {
                p.setPaCompanionId(pDto.getCompanion().getItem_ID());
            }
            
            // 양방향 관계 설정
            p.setGameInfo(game);
            game.getParticipants().add(p);

            // 4. 시너지(Traits) 순회
            if (pDto.getTraits() != null) {
                for (TraitDto tDto : pDto.getTraits()) {
                    // 활성화된 시너지만 저장 (tier_current가 0보다 클 때)
                    if (tDto.getTier_current() > 0) { 
                        Trait t = new Trait();
                        t.setTrName(tDto.getName());
                        t.setTrNumUnits(tDto.getNum_units());
                        t.setTrStyle(tDto.getStyle());
                        
                        t.setParticipant(p);
                        p.getTraits().add(t);
                    }
                }
            }

            // 5. 유닛(Units) 순회
            if (pDto.getUnits() != null) {
                for (UnitDto uDto : pDto.getUnits()) {
                    Unit u = new Unit();
                    u.setUnId(uDto.getCharacterId());
                    u.setUnTier(uDto.getStar());
                    u.setUnName(uDto.getCharacterId());
                    u.setUnCost(uDto.getCost());
                    u.setParticipant(p);
                    p.getUnits().add(u);

                    // 6. 아이템 저장 (1:1 관계)
                    // DTO의 아이템 리스트 필드명이 getItems() 인지 확인해주세요!
                    if (uDto.getItems() != null && !uDto.getItems().isEmpty()) {
                        Item item = new Item();
                        List<String> itemList = uDto.getItems();
                        
                        item.setItFirst(itemList.size() > 0 ? itemList.get(0) : null);
                        item.setItSecond(itemList.size() > 1 ? itemList.get(1) : null);
                        item.setItThird(itemList.size() > 2 ? itemList.get(2) : null);
                        
                        item.setUnit(u);
                        u.setItem(item);
                    }
                }
            }
        }

        // 7. DB 저장 (Cascade에 의해 하위 객체들 자동 INSERT)
        gameInfoRepository.save(game);
    }
}