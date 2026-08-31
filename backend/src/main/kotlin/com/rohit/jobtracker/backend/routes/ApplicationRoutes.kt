package com.rohit.jobtracker.backend.routes

import com.rohit.jobtracker.backend.repository.JobTrackerRepository
import com.rohit.jobtracker.shared.model.CreateApplicationRequest
import com.rohit.jobtracker.shared.model.CreateNoteRequest
import com.rohit.jobtracker.shared.model.UpdateApplicationRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureApplicationRoutes(repository: JobTrackerRepository) {
    routing {
        route("/applications") {
            // GET /applications - list all applications
            get {
                val applications = repository.getApplications()
                call.respond(HttpStatusCode.OK, applications)
            }

            // POST /applications - create new application
            post {
                val request = call.receive<CreateApplicationRequest>()
                if (request.company.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Company name cannot be blank"))
                    return@post
                }
                if (request.role.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Role cannot be blank"))
                    return@post
                }

                val created = repository.createApplication(request)
                call.respond(HttpStatusCode.Created, created)
            }

            // GET /applications/{id} - get single application
            get("/{id}") {
                val id = call.parameters["id"] ?: run {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing application id"))
                    return@get
                }

                val application = repository.getApplication(id)
                if (application != null) {
                    call.respond(HttpStatusCode.OK, application)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Application with id '$id' not found"))
                }
            }

            // PATCH /applications/{id} - update application fields
            patch("/{id}") {
                val id = call.parameters["id"] ?: run {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing application id"))
                    return@patch
                }

                val request = call.receive<UpdateApplicationRequest>()
                val updated = repository.updateApplication(id, request)
                if (updated != null) {
                    call.respond(HttpStatusCode.OK, updated)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Application with id '$id' not found"))
                }
            }

            // DELETE /applications/{id} - delete application
            delete("/{id}") {
                val id = call.parameters["id"] ?: run {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing application id"))
                    return@delete
                }

                val deleted = repository.deleteApplication(id)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("success" to true))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Application with id '$id' not found"))
                }
            }

            // GET /applications/{id}/notes - list notes for application
            get("/{id}/notes") {
                val id = call.parameters["id"] ?: run {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing application id"))
                    return@get
                }

                val app = repository.getApplication(id)
                if (app == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Application with id '$id' not found"))
                    return@get
                }

                val notes = repository.getNotes(id)
                call.respond(HttpStatusCode.OK, notes)
            }

            // POST /applications/{id}/notes - add note to application
            post("/{id}/notes") {
                val id = call.parameters["id"] ?: run {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing application id"))
                    return@post
                }

                val request = call.receive<CreateNoteRequest>()
                if (request.text.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Note text cannot be blank"))
                    return@post
                }

                val note = repository.addNote(id, request)
                if (note != null) {
                    call.respond(HttpStatusCode.Created, note)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Application with id '$id' not found"))
                }
            }
        }
    }
}
