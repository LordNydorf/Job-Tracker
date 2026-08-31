package com.rohit.jobtracker.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class Source {
    WELLFOUND,
    UPWORK,
    CONTRA,
    BRAINTRUST,
    TOPTAL,
    REFERRAL,
    OTHER
}
