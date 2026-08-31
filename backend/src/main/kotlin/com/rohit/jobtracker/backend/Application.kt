package com.rohit.jobtracker.backend

import com.rohit.jobtracker.backend.db.DatabaseFactory
import com.rohit.jobtracker.backend.plugins.configureSecurity
import com.rohit.jobtracker.backend.plugins.configureStatusPages
import com.rohit.jobtracker.backend.repository.JobTrackerRepository
import com.rohit.jobtracker.backend.repository.JobTrackerRepositoryImpl
import com.rohit.jobtracker.backend.routes.configureApplicationRoutes
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(
    repository: JobTrackerRepository = JobTrackerRepositoryImpl(),
    initDb: Boolean = true
) {
    if (initDb) {
        val jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:sqlite:data/jobtracker.db"
        DatabaseFactory.init(jdbcUrl = jdbcUrl)
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
        })
    }

    configureStatusPages()
    configureSecurity()

    routing {
        get("/") {
            call.respondText("Job Application Tracker API is running!", ContentType.Text.Plain)
        }
        get("/health") {
            call.respond(mapOf("status" to "ok", "service" to "job-tracker-backend"))
        }
    }

    configureApplicationRoutes(repository)
}
