package com.arenita.tracking.infrastructure.config

import com.arenita.tracking.domain.model.AiProvider
import com.arenita.tracking.domain.model.ModelPricing
import com.arenita.tracking.domain.repository.ModelPricingRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

@Configuration
class DataSeeder {

    @Bean
    fun seedPricing(repo: ModelPricingRepository) = CommandLineRunner {
        if (repo.count() == 0L) {
            repo.saveAll(listOf(
                ModelPricing(modelName = "claude-opus-4", provider = AiProvider.ANTHROPIC,
                    inputPricePerMillionTokens = BigDecimal("15"), outputPricePerMillionTokens = BigDecimal("75"),
                    cacheReadPricePerMillionTokens = BigDecimal("1.5"), cacheWritePricePerMillionTokens = BigDecimal("18.75")),
                ModelPricing(modelName = "claude-sonnet-4", provider = AiProvider.ANTHROPIC,
                    inputPricePerMillionTokens = BigDecimal("3"), outputPricePerMillionTokens = BigDecimal("15"),
                    cacheReadPricePerMillionTokens = BigDecimal("0.3"), cacheWritePricePerMillionTokens = BigDecimal("3.75")),
                ModelPricing(modelName = "claude-haiku-3.5", provider = AiProvider.ANTHROPIC,
                    inputPricePerMillionTokens = BigDecimal("0.8"), outputPricePerMillionTokens = BigDecimal("4"),
                    cacheReadPricePerMillionTokens = BigDecimal("0.08"), cacheWritePricePerMillionTokens = BigDecimal("1")),
                ModelPricing(modelName = "gpt-4o", provider = AiProvider.OPENAI,
                    inputPricePerMillionTokens = BigDecimal("2.5"), outputPricePerMillionTokens = BigDecimal("10")),
                ModelPricing(modelName = "gpt-4o-mini", provider = AiProvider.OPENAI,
                    inputPricePerMillionTokens = BigDecimal("0.15"), outputPricePerMillionTokens = BigDecimal("0.6")),
                ModelPricing(modelName = "gemini-2.0-flash", provider = AiProvider.GOOGLE,
                    inputPricePerMillionTokens = BigDecimal("0.1"), outputPricePerMillionTokens = BigDecimal("0.4"))
            ))
        }
    }
}
