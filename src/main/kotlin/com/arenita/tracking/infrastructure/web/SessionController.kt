package com.arenita.tracking.infrastructure.web

import com.arenita.tracking.api.dto.*
import com.arenita.tracking.application.SessionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Sessions", description = "Session tracking endpoints")
class SessionController(private val sessionService: SessionService) {

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record a session event")
    fun createSession(@Valid @RequestBody req: CreateSessionRequest): SessionResponse =
        sessionService.createSession(req)

    @GetMapping("/sessions")
    @Operation(summary = "List sessions with optional filters")
    fun getSessions(
        @RequestParam(required = false) userId: String?,
        @RequestParam(required = false) from: Instant?,
        @RequestParam(required = false) to: Instant?
    ): List<SessionResponse> = sessionService.getSessions(userId, from, to)

    @PutMapping("/sessions/{id}/purpose")
    @Operation(summary = "Manually override session purpose")
    fun updatePurpose(
        @PathVariable id: UUID,
        @Valid @RequestBody req: UpdatePurposeRequest
    ): SessionResponse = sessionService.updatePurpose(id, req.purpose)

    @GetMapping("/users/{userId}/summary")
    @Operation(summary = "Get user usage summary")
    fun getUserSummary(@PathVariable userId: String): UserSummaryResponse =
        sessionService.getUserSummary(userId)

    @GetMapping("/users/{userId}/costs")
    @Operation(summary = "Get user cost breakdown")
    fun getUserCosts(
        @PathVariable userId: String,
        @RequestParam(required = false) from: Instant?,
        @RequestParam(required = false) to: Instant?
    ): CostBreakdownResponse = sessionService.getUserCosts(userId, from, to)

    @GetMapping("/analytics/overview")
    @Operation(summary = "Global analytics dashboard")
    fun getOverview(): AnalyticsOverviewResponse = sessionService.getAnalyticsOverview()

    @GetMapping("/analytics/purposes")
    @Operation(summary = "Purpose distribution")
    fun getPurposes(@RequestParam(required = false) userId: String?): PurposeDistributionResponse =
        sessionService.getPurposeDistribution(userId)
}
