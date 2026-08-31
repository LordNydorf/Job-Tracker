package com.rohit.jobtracker.backend.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.request.path
import io.ktor.server.response.respond

val ApiKeyAuth = createApplicationPlugin(name = "ApiKeyAuth") {
    val expectedApiKey = System.getenv("JOB_TRACKER_API_KEY") ?: "dev-secret-key"

    onCall { call ->
        val path = call.request.path()
        // Public endpoints bypass API key check
        if (path == "/" || path == "/health") {
            return@onCall
        }

        val providedKey = call.request.headers["X-API-Key"]
        if (providedKey == null || providedKey != expectedApiKey) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or missing X-API-Key header"))
        }
    }
}

fun Application.configureSecurity() {
    install(ApiKeyAuth)
}
