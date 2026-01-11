package com.tft.web.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tft.web.domain.Participant;
import com.tft.web.model.dto.MatchApiDto;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Integer> {

        List<Participant> findByPaPuuidAndGameInfo_GaDatetimeAfter(String puuid, LocalDateTime of);

    

        List<Participant> findByPaPuuid(String puuid);

    

        Page<MatchApiDto> findByPaPuuidOrderByGameInfo_GaDatetimeDesc(String puuid, Pageable pageable);

        

        int countByPaPuuid(String puuid);

        @org.springframework.data.jpa.repository.Query("SELECT p FROM Participant p WHERE p.paName = :name ORDER BY p.gameInfo.gaDatetime DESC")
        List<Participant> findByPaName(String name);
    }