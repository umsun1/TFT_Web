package com.tft.web.repository;

import com.tft.web.domain.GameInfo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameInfoRepository extends JpaRepository<GameInfo, Integer> {
    // GA_ID(라이엇 매치 아이디)로 중복 여부 확인
    boolean existsByGaId(String gaId);

    Optional<GameInfo> findByGaId(String matchId);
}