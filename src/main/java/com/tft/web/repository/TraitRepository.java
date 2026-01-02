package com.tft.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tft.web.domain.Trait;

@Repository
public interface TraitRepository extends JpaRepository<Trait, Integer> {
    
}