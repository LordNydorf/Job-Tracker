package com.rohit.jobtracker.shared.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Application(
    val id: String,
    val company: String,
    val role: String,
    val source: Source,
    val dateApplied: LocalDate,
    val jobLink: String? = null,
    val status: Status = Status.APPLIED,
    val lastUpdated: Instant,
    val reminderDays: Int? = null,
    val salary: String? = null
)
