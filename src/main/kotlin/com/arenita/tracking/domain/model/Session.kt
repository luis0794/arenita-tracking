package com.arenita.tracking.domain.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "sessions")
data class Session(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    val userId: String,

    @Column(nullable = false)
    val sessionKey: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val provider: AiProvider,

    @Column(nullable = false)
    val model: String,

    val tokensIn: Long = 0,
    val tokensOut: Long = 0,
    val cacheHits: Long = 0,
    val cacheMisses: Long = 0,

    @Column(precision = 10, scale = 6)
    val estimatedCost: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val purpose: ConversationPurpose = ConversationPurpose.UNKNOWN,

    val purposeManualOverride: Boolean = false,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "session_data_types", joinColumns = [JoinColumn(name = "session_id")])
    @Enumerated(EnumType.STRING)
    val dataTypes: Set<DataType> = emptySet(),

    val mediaSizeBytes: Long = 0,
    val messageCount: Int = 0,

    @Column(nullable = false)
    val startedAt: Instant = Instant.now(),

    val endedAt: Instant? = null,

    val avgResponseTimeMs: Long? = null,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)

enum class AiProvider { ANTHROPIC, OPENAI, GOOGLE }
enum class ConversationPurpose { WORK, PERSONAL, EMOTIONAL, TECHNICAL, CREATIVE, UNKNOWN }
enum class DataType { TEXT, PDF, IMAGE, CSV, VOICE, VIDEO, DOCUMENT }
