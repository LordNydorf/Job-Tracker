package com.rohit.jobtracker.android.network

import com.rohit.jobtracker.android.BuildConfig
import com.rohit.jobtracker.shared.api.JobTrackerApi
import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.CreateApplicationRequest
import com.rohit.jobtracker.shared.model.CreateNoteRequest
import com.rohit.jobtracker.shared.model.Note
import com.rohit.jobtracker.shared.model.UpdateApplicationRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class KtorJobTrackerApi(
    private val client: HttpClient,
    private val serverConfig: ServerConfig
) : JobTrackerApi {

    private val baseUrl: String get() = serverConfig.getBaseUrl()
    private val apiKey: String get() = serverConfig.getApiKey()

    override suspend fun getApplications(): List<Application> {
        val response = client.get("$baseUrl/applications") {
            header("X-API-Key", apiKey)
        }
        if (response.status == HttpStatusCode.OK) {
            return response.body()
        }
        throw Exception("Failed to load applications (${response.status.value}): ${response.status.description}")
    }

    override suspend fun getApplication(id: String): Application? {
        val response = client.get("$baseUrl/applications/$id") {
            header("X-API-Key", apiKey)
        }
        return when (response.status) {
            HttpStatusCode.OK -> response.body()
            HttpStatusCode.NotFound -> null
            else -> throw Exception("Failed to load application $id (${response.status.value})")
        }
    }

    override suspend fun createApplication(request: CreateApplicationRequest): Application {
        val response = client.post("$baseUrl/applications") {
            header("X-API-Key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
            return response.body()
        }
        throw Exception("Failed to create application (${response.status.value})")
    }

    override suspend fun updateApplication(id: String, request: UpdateApplicationRequest): Application {
        val response = client.patch("$baseUrl/applications/$id") {
            header("X-API-Key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (response.status == HttpStatusCode.OK) {
            return response.body()
        }
        throw Exception("Failed to update application $id (${response.status.value})")
    }

    override suspend fun deleteApplication(id: String): Boolean {
        val response = client.delete("$baseUrl/applications/$id") {
            header("X-API-Key", apiKey)
        }
        return response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NoContent
    }

    override suspend fun getNotes(applicationId: String): List<Note> {
        val response = client.get("$baseUrl/applications/$applicationId/notes") {
            header("X-API-Key", apiKey)
        }
        if (response.status == HttpStatusCode.OK) {
            return response.body()
        }
        if (response.status == HttpStatusCode.NotFound) {
            return emptyList()
        }
        throw Exception("Failed to load notes (${response.status.value})")
    }

    override suspend fun addNote(applicationId: String, request: CreateNoteRequest): Note {
        val response = client.post("$baseUrl/applications/$applicationId/notes") {
            header("X-API-Key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
            return response.body()
        }
        throw Exception("Failed to add note (${response.status.value})")
    }

    override suspend fun deleteNote(applicationId: String, noteId: String): Boolean {
        val response = client.delete("$baseUrl/applications/$applicationId/notes/$noteId") {
            header("X-API-Key", apiKey)
        }
        return response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NoContent
    }
}
