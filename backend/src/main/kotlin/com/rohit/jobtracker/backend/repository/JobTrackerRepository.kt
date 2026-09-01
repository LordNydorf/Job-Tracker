package com.rohit.jobtracker.backend.repository

import com.rohit.jobtracker.backend.db.ApplicationsTable
import com.rohit.jobtracker.backend.db.DatabaseFactory.dbQuery
import com.rohit.jobtracker.backend.db.NotesTable
import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.CreateApplicationRequest
import com.rohit.jobtracker.shared.model.CreateNoteRequest
import com.rohit.jobtracker.shared.model.Note
import com.rohit.jobtracker.shared.model.Source
import com.rohit.jobtracker.shared.model.Status
import com.rohit.jobtracker.shared.model.UpdateApplicationRequest
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID

interface JobTrackerRepository {
    suspend fun getApplications(): List<Application>
    suspend fun getApplication(id: String): Application?
    suspend fun createApplication(request: CreateApplicationRequest): Application
    suspend fun updateApplication(id: String, request: UpdateApplicationRequest): Application?
    suspend fun deleteApplication(id: String): Boolean
    suspend fun getNotes(applicationId: String): List<Note>
    suspend fun addNote(applicationId: String, request: CreateNoteRequest): Note?
    suspend fun deleteNote(applicationId: String, noteId: String): Boolean
}

class JobTrackerRepositoryImpl : JobTrackerRepository {

    private fun resultRowToApplication(row: ResultRow) = Application(
        id = row[ApplicationsTable.id],
        company = row[ApplicationsTable.company],
        role = row[ApplicationsTable.role],
        source = Source.valueOf(row[ApplicationsTable.appSource]),
        dateApplied = row[ApplicationsTable.dateApplied],
        jobLink = row[ApplicationsTable.jobLink],
        status = Status.valueOf(row[ApplicationsTable.status]),
        lastUpdated = row[ApplicationsTable.lastUpdated],
        reminderDays = row[ApplicationsTable.reminderDays],
        salary = row[ApplicationsTable.salary]
    )

    private fun resultRowToNote(row: ResultRow) = Note(
        id = row[NotesTable.id],
        applicationId = row[NotesTable.applicationId],
        text = row[NotesTable.text],
        createdAt = row[NotesTable.createdAt]
    )

    override suspend fun getApplications(): List<Application> = dbQuery {
        ApplicationsTable
            .selectAll()
            .orderBy(ApplicationsTable.lastUpdated, SortOrder.DESC)
            .map(::resultRowToApplication)
    }

    override suspend fun getApplication(id: String): Application? = dbQuery {
        ApplicationsTable
            .selectAll()
            .where { ApplicationsTable.id eq id }
            .map(::resultRowToApplication)
            .singleOrNull()
    }

    override suspend fun createApplication(request: CreateApplicationRequest): Application = dbQuery {
        val newId = request.id?.trim()?.takeIf { it.isNotEmpty() } ?: UUID.randomUUID().toString()
        val now = Clock.System.now()
        val trimmedSalary = request.salary?.trim()?.takeIf { s -> s.isNotEmpty() }

        ApplicationsTable.insert {
            it[id] = newId
            it[company] = request.company.trim()
            it[role] = request.role.trim()
            it[appSource] = request.source.name
            it[dateApplied] = request.dateApplied
            it[jobLink] = request.jobLink?.trim()?.takeIf { link -> link.isNotEmpty() }
            it[status] = request.status.name
            it[lastUpdated] = now
            it[reminderDays] = request.reminderDays
            it[salary] = trimmedSalary
        }

        Application(
            id = newId,
            company = request.company.trim(),
            role = request.role.trim(),
            source = request.source,
            dateApplied = request.dateApplied,
            jobLink = request.jobLink?.trim()?.takeIf { link -> link.isNotEmpty() },
            status = request.status,
            lastUpdated = now,
            reminderDays = request.reminderDays,
            salary = trimmedSalary
        )
    }

    override suspend fun updateApplication(id: String, request: UpdateApplicationRequest): Application? = dbQuery {
        val existing = ApplicationsTable
            .selectAll()
            .where { ApplicationsTable.id eq id }
            .singleOrNull() ?: return@dbQuery null

        val now = Clock.System.now()

        ApplicationsTable.update({ ApplicationsTable.id eq id }) {
            request.company?.let { comp -> it[company] = comp.trim() }
            request.role?.let { r -> it[role] = r.trim() }
            request.source?.let { s -> it[appSource] = s.name }
            request.dateApplied?.let { d -> it[dateApplied] = d }
            request.jobLink?.let { link -> it[jobLink] = link.trim().takeIf { l -> l.isNotEmpty() } }
            request.status?.let { st -> it[status] = st.name }
            if (request.reminderDays != null) {
                it[reminderDays] = request.reminderDays
            }
            request.salary?.let { sal ->
                it[salary] = sal.trim().takeIf { s -> s.isNotEmpty() }
            }
            it[lastUpdated] = now
        }

        ApplicationsTable
            .selectAll()
            .where { ApplicationsTable.id eq id }
            .map(::resultRowToApplication)
            .singleOrNull()
    }

    override suspend fun deleteApplication(id: String): Boolean = dbQuery {
        val deletedRows = ApplicationsTable.deleteWhere { ApplicationsTable.id eq id }
        deletedRows > 0
    }

    override suspend fun getNotes(applicationId: String): List<Note> = dbQuery {
        val appExists = ApplicationsTable
            .selectAll()
            .where { ApplicationsTable.id eq applicationId }
            .count() > 0

        if (!appExists) return@dbQuery emptyList()

        NotesTable
            .selectAll()
            .where { NotesTable.applicationId eq applicationId }
            .orderBy(NotesTable.createdAt, SortOrder.DESC)
            .map(::resultRowToNote)
    }

    override suspend fun addNote(applicationId: String, request: CreateNoteRequest): Note? = dbQuery {
        val appExists = ApplicationsTable
            .selectAll()
            .where { ApplicationsTable.id eq applicationId }
            .count() > 0

        if (!appExists) return@dbQuery null

        val noteId = request.id?.trim()?.takeIf { it.isNotEmpty() } ?: UUID.randomUUID().toString()
        val now = Clock.System.now()

        NotesTable.insert {
            it[id] = noteId
            it[NotesTable.applicationId] = applicationId
            it[text] = request.text.trim()
            it[createdAt] = now
        }

        // Update parent application's lastUpdated timestamp
        ApplicationsTable.update({ ApplicationsTable.id eq applicationId }) {
            it[lastUpdated] = now
        }

        Note(
            id = noteId,
            applicationId = applicationId,
            text = request.text.trim(),
            createdAt = now
        )
    }

    override suspend fun deleteNote(applicationId: String, noteId: String): Boolean = dbQuery {
        val deletedRows = NotesTable.deleteWhere {
            (NotesTable.id eq noteId) and (NotesTable.applicationId eq applicationId)
        }
        deletedRows > 0
    }
}
