package com.tft.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tft.web.domain.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    
}