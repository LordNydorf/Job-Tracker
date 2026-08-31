package com.rohit.jobtracker.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class Status {
    APPLIED,
    SCREENING,
    INTERVIEW,
    OFFER,
    REJECTED,
    GHOSTED
}
