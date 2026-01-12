package com.tft.web.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meta_deck")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaDeck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 덱 이름 (예: 7 펜타킬)
    
    @Column(columnDefinition = "TEXT")
    private String coreUnits; // 핵심 유닛 리스트 (JSON 문자열)
    
    @Column(columnDefinition = "TEXT")
    private String traits; // 활성 시너지 리스트 (JSON 문자열)
    
    private double avgPlacement; // 평균 등수
    private double winRate; // 승률 (1등 비율)
    private double top4Rate; // 순방 비율
    private double pickRate; // 픽률 (전체 게임 대비)
    
    private String tier; // S, A, B, C

    private LocalDateTime updatedAt;
}
