package com.tft.web.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "trait")
public class Trait {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TR_NUM")
    private Integer trNum;

    @Column(name = "TR_NAME", nullable = false, length = 50)
    private String trName;

    @Column(name = "TR_NUM_UNITS", nullable = false)
    private Integer trNumUnits;

    @Column(name = "TR_STYLE", nullable = false)
    private Integer trStyle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TR_PA_NUM")
    private Participant participant;
}