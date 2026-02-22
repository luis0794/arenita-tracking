package com.arenita.tracking.infrastructure.web

import com.arenita.tracking.api.dto.WebhookEvent
import com.arenita.tracking.application.WebhookService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/webhook")
@Tag(name = "Webhook", description = "OpenClaw webhook integration")
class WebhookController(private val webhookService: WebhookService) {

    @PostMapping("/openclaw")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Receive events from OpenClaw gateway")
    fun receiveEvent(@RequestBody event: WebhookEvent) {
        webhookService.processEvent(event)
    }
}
