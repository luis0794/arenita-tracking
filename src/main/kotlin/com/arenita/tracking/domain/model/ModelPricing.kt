package com.arenita.tracking.domain.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "model_pricing")
data class ModelPricing(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val modelName: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val provider: AiProvider,

    @Column(precision = 12, scale = 8)
    val inputPricePerMillionTokens: BigDecimal,

    @Column(precision = 12, scale = 8)
    val outputPricePerMillionTokens: BigDecimal,

    @Column(precision = 12, scale = 8)
    val cacheReadPricePerMillionTokens: BigDecimal = BigDecimal.ZERO,

    @Column(precision = 12, scale = 8)
    val cacheWritePricePerMillionTokens: BigDecimal = BigDecimal.ZERO
)
