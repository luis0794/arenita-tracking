package com.arenita.tracking.application

import com.arenita.tracking.api.dto.*
import com.arenita.tracking.domain.model.*
import com.arenita.tracking.domain.repository.ModelPricingRepository
import com.arenita.tracking.domain.repository.SessionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class SessionService(
    private val sessionRepository: SessionRepository,
    private val pricingRepository: ModelPricingRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createSession(req: CreateSessionRequest): SessionResponse {
        val pricing = pricingRepository.findByModelName(req.model)
        val cost = calculateCost(req, pricing)

        val session = Session(
            userId = req.userId,
            sessionKey = req.sessionKey,
            provider = AiProvider.valueOf(req.provider.uppercase()),
            model = req.model,
            tokensIn = req.tokensIn,
            tokensOut = req.tokensOut,
            cacheHits = req.cacheHits,
            cacheMisses = req.cacheMisses,
            estimatedCost = cost,
            purpose = req.purpose?.let { ConversationPurpose.valueOf(it.uppercase()) } ?: ConversationPurpose.UNKNOWN,
            dataTypes = req.dataTypes.map { DataType.valueOf(it.uppercase()) }.toSet(),
            mediaSizeBytes = req.mediaSizeBytes,
            messageCount = req.messageCount,
            startedAt = req.startedAt ?: Instant.now(),
            endedAt = req.endedAt,
            avgResponseTimeMs = req.avgResponseTimeMs
        )
        return sessionRepository.save(session).toResponse().also {
            log.info("Session created: user=${req.userId} model=${req.model} cost=$cost")
        }
    }

    fun getSessions(userId: String?, from: Instant?, to: Instant?): List<SessionResponse> {
        val sessions = when {
            userId != null && from != null && to != null ->
                sessionRepository.findByUserIdAndCreatedAtBetween(userId, from, to)
            userId != null -> sessionRepository.findByUserId(userId)
            from != null && to != null -> sessionRepository.findByCreatedAtBetween(from, to)
            else -> sessionRepository.findAll()
        }
        return sessions.map { it.toResponse() }
    }

    fun getUserSummary(userId: String): UserSummaryResponse {
        val sessions = sessionRepository.findByUserId(userId)
        val purposeDist = sessions.groupBy { it.purpose.name }.mapValues { it.value.size.toLong() }
        val dataTypeDist = sessions.flatMap { it.dataTypes }.groupBy { it.name }.mapValues { it.value.size.toLong() }
        val avgDuration = sessions.mapNotNull { s ->
            s.endedAt?.let { it.toEpochMilli() - s.startedAt.toEpochMilli() }
        }.takeIf { it.isNotEmpty() }?.average()?.toLong()

        return UserSummaryResponse(
            userId = userId,
            totalSessions = sessions.size.toLong(),
            totalCost = sessions.sumOf { it.estimatedCost },
            totalTokensIn = sessions.sumOf { it.tokensIn },
            totalTokensOut = sessions.sumOf { it.tokensOut },
            totalMessages = sessions.sumOf { it.messageCount },
            purposeDistribution = purposeDist,
            dataTypeDistribution = dataTypeDist,
            avgSessionDurationMs = avgDuration
        )
    }

    fun getUserCosts(userId: String, from: Instant?, to: Instant?): CostBreakdownResponse {
        val start = from ?: Instant.EPOCH
        val end = to ?: Instant.now()
        val sessions = sessionRepository.findByUserIdAndCreatedAtBetween(userId, start, end)
        
        return CostBreakdownResponse(
            userId = userId,
            period = "${start}/${end}",
            totalCost = sessions.sumOf { it.estimatedCost },
            costByProvider = sessions.groupBy { it.provider.name }.mapValues { e -> e.value.sumOf { it.estimatedCost } },
            costByModel = sessions.groupBy { it.model }.mapValues { e -> e.value.sumOf { it.estimatedCost } },
            sessions = sessions.map { it.toResponse() }
        )
    }

    fun getAnalyticsOverview(): AnalyticsOverviewResponse {
        val allSessions = sessionRepository.findAll()
        val now = Instant.now()
        val thirtyDaysAgo = now.minusSeconds(30 * 24 * 3600)
        val recentSummaries = sessionRepository.getUserSummaries(thirtyDaysAgo, now)

        val totalCost = allSessions.sumOf { it.estimatedCost }
        val sessionsByDay = allSessions.groupBy {
            LocalDate.ofInstant(it.createdAt, ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE)
        }.mapValues { it.value.size.toLong() }

        return AnalyticsOverviewResponse(
            totalUsers = allSessions.map { it.userId }.distinct().size.toLong(),
            totalSessions = allSessions.size.toLong(),
            totalCost = totalCost,
            topUsers = recentSummaries.map { row ->
                UserCostSummary(
                    userId = row[0] as String,
                    sessionCount = row[1] as Long,
                    totalCost = row[2] as BigDecimal
                )
            },
            sessionsByDay = sessionsByDay,
            avgCostPerSession = if (allSessions.isNotEmpty())
                totalCost.divide(BigDecimal(allSessions.size), 6, RoundingMode.HALF_UP)
            else BigDecimal.ZERO
        )
    }

    fun getPurposeDistribution(userId: String?): PurposeDistributionResponse {
        val rows = if (userId != null)
            sessionRepository.countByUserIdGroupByPurpose(userId)
        else
            sessionRepository.countGroupByPurpose()

        val dist = rows.associate { (it[0] as ConversationPurpose).name to it[1] as Long }
        return PurposeDistributionResponse(dist, dist.values.sum())
    }

    @Transactional
    fun updatePurpose(sessionId: UUID, purpose: String): SessionResponse {
        val session = sessionRepository.findById(sessionId).orElseThrow { NoSuchElementException("Session not found") }
        val updated = session.copy(
            purpose = ConversationPurpose.valueOf(purpose.uppercase()),
            purposeManualOverride = true
        )
        return sessionRepository.save(updated).toResponse()
    }

    private fun calculateCost(req: CreateSessionRequest, pricing: ModelPricing?): BigDecimal {
        if (pricing == null) return BigDecimal.ZERO
        val million = BigDecimal(1_000_000)
        val inputCost = BigDecimal(req.tokensIn).multiply(pricing.inputPricePerMillionTokens).divide(million, 8, RoundingMode.HALF_UP)
        val outputCost = BigDecimal(req.tokensOut).multiply(pricing.outputPricePerMillionTokens).divide(million, 8, RoundingMode.HALF_UP)
        val cacheCost = BigDecimal(req.cacheHits).multiply(pricing.cacheReadPricePerMillionTokens).divide(million, 8, RoundingMode.HALF_UP)
        return inputCost.add(outputCost).add(cacheCost)
    }
}
