package com.rohit.jobtracker.shared.api

import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.CreateApplicationRequest
import com.rohit.jobtracker.shared.model.CreateNoteRequest
import com.rohit.jobtracker.shared.model.Note
import com.rohit.jobtracker.shared.model.UpdateApplicationRequest

interface JobTrackerApi {
    suspend fun getApplications(): List<Application>
    suspend fun getApplication(id: String): Application?
    suspend fun createApplication(request: CreateApplicationRequest): Application
    suspend fun updateApplication(id: String, request: UpdateApplicationRequest): Application
    suspend fun deleteApplication(id: String): Boolean
    suspend fun getNotes(applicationId: String): List<Note>
    suspend fun addNote(applicationId: String, request: CreateNoteRequest): Note
}
