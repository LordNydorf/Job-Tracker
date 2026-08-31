package com.rohit.jobtracker.shared.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val applicationId: String,
    val text: String,
    val createdAt: Instant
)
