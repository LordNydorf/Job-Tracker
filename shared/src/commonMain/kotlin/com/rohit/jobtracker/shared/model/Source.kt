package com.rohit.jobtracker.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class Source(val displayName: String) {
    WELLFOUND("Wellfound"),
    UPWORK("Upwork"),
    CONTRA("Contra"),
    BRAINTRUST("Braintrust"),
    TOPTAL("Toptal"),
    REFERRAL("Referral"),
    OTHER("Other")
}
