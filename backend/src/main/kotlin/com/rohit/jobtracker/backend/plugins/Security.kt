package com.rohit.jobtracker.backend.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.request.path
import io.ktor.server.response.respond
import java.security.MessageDigest

fun Application.configureSecurity(apiKey: String? = null) {
    val isDevMode = System.getenv("ENVIRONMENT") == "development" || System.getProperty("io.ktor.development") == "true" || apiKey != null
    val configuredKey = apiKey
        ?: System.getenv("API_KEY")
        ?: System.getenv("JOB_TRACKER_API_KEY")
        ?: if (isDevMode) {
            "dev-secret-key"
        } else {
            throw IllegalStateException(
                "CRITICAL SECURITY CONFIG ERROR: 'API_KEY' environment variable is not configured. Server startup aborted."
            )
        }

    val apiKeyAuthPlugin = createApplicationPlugin(name = "ApiKeyAuthPlugin") {
        onCall { call ->
            val path = call.request.path()
            // Public health and root endpoints bypass auth check
            if (path == "/" || path == "/health") {
                return@onCall
            }

            val providedKey = call.request.headers["X-API-Key"]
            val isKeyValid = providedKey != null && MessageDigest.isEqual(
                providedKey.toByteArray(Charsets.UTF_8),
                configuredKey.toByteArray(Charsets.UTF_8)
            )
            if (!isKeyValid) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Invalid or missing X-API-Key header")
                )
            }
        }
    }

    install(apiKeyAuthPlugin)
}
