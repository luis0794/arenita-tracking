package com.arenita.tracking.domain.repository

import com.arenita.tracking.domain.model.ConversationPurpose
import com.arenita.tracking.domain.model.Session
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface SessionRepository : JpaRepository<Session, UUID> {
    fun findByUserId(userId: String): List<Session>
    fun findByUserIdAndCreatedAtBetween(userId: String, from: Instant, to: Instant): List<Session>
    fun findByCreatedAtBetween(from: Instant, to: Instant): List<Session>

    @Query("SELECT s.purpose, COUNT(s) FROM Session s WHERE s.userId = :userId GROUP BY s.purpose")
    fun countByUserIdGroupByPurpose(userId: String): List<Array<Any>>

    @Query("SELECT s.purpose, COUNT(s) FROM Session s GROUP BY s.purpose")
    fun countGroupByPurpose(): List<Array<Any>>

    @Query("SELECT s.userId, COUNT(s), SUM(s.estimatedCost) FROM Session s WHERE s.createdAt BETWEEN :from AND :to GROUP BY s.userId ORDER BY SUM(s.estimatedCost) DESC")
    fun getUserSummaries(from: Instant, to: Instant): List<Array<Any>>

    @Query("SELECT COUNT(s) FROM Session s WHERE s.userId = :userId")
    fun countByUserId(userId: String): Long
}
