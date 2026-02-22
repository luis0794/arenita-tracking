package com.arenita.tracking.domain.repository

import com.arenita.tracking.domain.model.ModelPricing
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ModelPricingRepository : JpaRepository<ModelPricing, UUID> {
    fun findByModelName(modelName: String): ModelPricing?
}
