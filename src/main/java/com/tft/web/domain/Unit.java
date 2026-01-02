package com.tft.web.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "unit")
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UN_NUM")
    private Integer unNum;

    @Column(name = "UN_ID", nullable = false, length = 50)
    private String unId;

    @Column(name = "UN_NAME", nullable = false, length = 50)
    private String unName;

    @Column(name = "UN_TIER", nullable = false)
    private Integer unTier;

    @Column(name = "UN_COST", nullable = false)
    private Integer unCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UN_PA_NUM")
    private Participant participant;

    // Item과 1:1 관계 (아이템 테이블 분리 반영)
    @OneToOne(mappedBy = "unit", cascade = CascadeType.ALL)
    private Item item;
}