package com.tft.web.repository;

import com.tft.web.domain.LpHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LpHistoryRepository extends JpaRepository<LpHistory, Long> {
    List<LpHistory> findTop15ByPuuidOrderByCreatedAtDesc(String puuid);
    
    // 가장 최근 기록 하나 가져오기
    LpHistory findTopByPuuidOrderByCreatedAtDesc(String puuid);
}
