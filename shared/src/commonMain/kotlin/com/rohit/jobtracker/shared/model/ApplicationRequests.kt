package com.rohit.jobtracker.shared.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class CreateApplicationRequest(
    val id: String? = null,
    val company: String,
    val role: String,
    val source: Source,
    val dateApplied: LocalDate,
    val jobLink: String? = null,
    val status: Status = Status.APPLIED,
    val reminderDays: Int? = null,
    val salary: String? = null
)

@Serializable
data class UpdateApplicationRequest(
    val company: String? = null,
    val role: String? = null,
    val source: Source? = null,
    val dateApplied: LocalDate? = null,
    val jobLink: String? = null,
    val status: Status? = null,
    val reminderDays: Int? = null,
    val salary: String? = null
)

@Serializable
data class CreateNoteRequest(
    val id: String? = null,
    val text: String
)
