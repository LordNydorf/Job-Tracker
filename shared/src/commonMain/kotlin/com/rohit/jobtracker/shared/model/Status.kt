package com.rohit.jobtracker.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class Status(val displayName: String) {
    APPLIED("Applied"),
    SCREENING("Screening"),
    INTERVIEW("Interview"),
    OFFER("Offer"),
    REJECTED("Rejected"),
    GHOSTED("Ghosted")
}
