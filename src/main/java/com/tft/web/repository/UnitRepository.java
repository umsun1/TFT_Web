package com.tft.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tft.web.domain.Unit;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Integer> {
    
}