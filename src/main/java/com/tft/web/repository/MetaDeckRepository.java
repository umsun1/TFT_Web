package com.tft.web.repository;

import com.tft.web.domain.MetaDeck;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MetaDeckRepository extends JpaRepository<MetaDeck, Long> {
    List<MetaDeck> findAllByOrderByAvgPlacementAsc(); // 등수가 낮은 순(좋은 순)으로 정렬
}
