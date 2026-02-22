package com.arenita.tracking.api.dto

import com.arenita.tracking.domain.model.*
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CreateSessionRequest(
    @field:NotBlank val userId: String,
    @field:NotBlank val sessionKey: String,
    @field:NotBlank val provider: String,
    @field:NotBlank val model: String,
    val tokensIn: Long = 0,
    val tokensOut: Long = 0,
    val cacheHits: Long = 0,
    val cacheMisses: Long = 0,
    val purpose: String? = null,
    val dataTypes: Set<String> = emptySet(),
    val mediaSizeBytes: Long = 0,
    val messageCount: Int = 0,
    val startedAt: Instant? = null,
    val endedAt: Instant? = null,
    val avgResponseTimeMs: Long? = null
)

data class SessionResponse(
    val id: UUID,
    val userId: String,
    val sessionKey: String,
    val provider: String,
    val model: String,
    val tokensIn: Long,
    val tokensOut: Long,
    val cacheHits: Long,
    val estimatedCost: BigDecimal,
    val purpose: String,
    val dataTypes: Set<String>,
    val mediaSizeBytes: Long,
    val messageCount: Int,
    val startedAt: Instant,
    val endedAt: Instant?,
    val avgResponseTimeMs: Long?,
    val createdAt: Instant
)

data class UpdatePurposeRequest(
    @field:NotBlank val purpose: String
)

data class UserSummaryResponse(
    val userId: String,
    val totalSessions: Long,
    val totalCost: BigDecimal,
    val totalTokensIn: Long,
    val totalTokensOut: Long,
    val totalMessages: Int,
    val purposeDistribution: Map<String, Long>,
    val dataTypeDistribution: Map<String, Long>,
    val avgSessionDurationMs: Long?
)

data class CostBreakdownResponse(
    val userId: String,
    val period: String,
    val totalCost: BigDecimal,
    val costByProvider: Map<String, BigDecimal>,
    val costByModel: Map<String, BigDecimal>,
    val sessions: List<SessionResponse>
)

data class AnalyticsOverviewResponse(
    val totalUsers: Long,
    val totalSessions: Long,
    val totalCost: BigDecimal,
    val topUsers: List<UserCostSummary>,
    val sessionsByDay: Map<String, Long>,
    val avgCostPerSession: BigDecimal
)

data class UserCostSummary(
    val userId: String,
    val sessionCount: Long,
    val totalCost: BigDecimal
)

data class PurposeDistributionResponse(
    val distribution: Map<String, Long>,
    val total: Long
)

data class WebhookEvent(
    val eventType: String,
    val sessionKey: String? = null,
    val userId: String? = null,
    val provider: String? = null,
    val model: String? = null,
    val tokensIn: Long? = null,
    val tokensOut: Long? = null,
    val cacheHits: Long? = null,
    val dataTypes: Set<String>? = null,
    val mediaSizeBytes: Long? = null,
    val messageCount: Int? = null,
    val purpose: String? = null,
    val timestamp: Instant? = null
)

fun Session.toResponse() = SessionResponse(
    id = id!!,
    userId = userId,
    sessionKey = sessionKey,
    provider = provider.name,
    model = model,
    tokensIn = tokensIn,
    tokensOut = tokensOut,
    cacheHits = cacheHits,
    estimatedCost = estimatedCost,
    purpose = purpose.name,
    dataTypes = dataTypes.map { it.name }.toSet(),
    mediaSizeBytes = mediaSizeBytes,
    messageCount = messageCount,
    startedAt = startedAt,
    endedAt = endedAt,
    avgResponseTimeMs = avgResponseTimeMs,
    createdAt = createdAt
)
