package com.arenita.tracking.application

import com.arenita.tracking.api.dto.CreateSessionRequest
import com.arenita.tracking.api.dto.WebhookEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WebhookService(private val sessionService: SessionService) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun processEvent(event: WebhookEvent) {
        log.info("Webhook event received: type=${event.eventType} user=${event.userId}")
        when (event.eventType) {
            "session.completed", "session.updated" -> {
                if (event.userId != null && event.sessionKey != null && event.provider != null && event.model != null) {
                    sessionService.createSession(
                        CreateSessionRequest(
                            userId = event.userId,
                            sessionKey = event.sessionKey,
                            provider = event.provider,
                            model = event.model,
                            tokensIn = event.tokensIn ?: 0,
                            tokensOut = event.tokensOut ?: 0,
                            cacheHits = event.cacheHits ?: 0,
                            dataTypes = event.dataTypes ?: emptySet(),
                            mediaSizeBytes = event.mediaSizeBytes ?: 0,
                            messageCount = event.messageCount ?: 0,
                            purpose = event.purpose
                        )
                    )
                }
            }
            else -> log.warn("Unknown webhook event type: ${event.eventType}")
        }
    }
}
