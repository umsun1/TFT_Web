package com.tft.web.repository;

import com.tft.web.domain.MatchFetchQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchFetchQueueRepository extends JpaRepository<MatchFetchQueue, Long> {
    Optional<MatchFetchQueue> findByMfqIdAndMfqType(String mfqId, String mfqType);
}
