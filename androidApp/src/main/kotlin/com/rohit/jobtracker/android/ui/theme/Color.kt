package com.rohit.jobtracker.android.ui.theme

import androidx.compose.ui.graphics.Color
import com.rohit.jobtracker.shared.model.Status

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Status Colors
val StatusAppliedText = Color(0xFF334155)
val StatusAppliedBg = Color(0xFFF1F5F9)

val StatusScreeningText = Color(0xFFB45309)
val StatusScreeningBg = Color(0xFFFEF3C7)

val StatusInterviewText = Color(0xFF4338CA)
val StatusInterviewBg = Color(0xFFEEF2FF)

val StatusOfferText = Color(0xFF047857)
val StatusOfferBg = Color(0xFFD1FAE5)

val StatusRejectedText = Color(0xFFB91C1C)
val StatusRejectedBg = Color(0xFFFEE2E2)

val StatusGhostedText = Color(0xFF475569)
val StatusGhostedBg = Color(0xFFF8FAFC)
val StatusGhostedBorder = Color(0xFF94A3B8)

fun Status.textColor(): Color = when (this) {
    Status.APPLIED -> StatusAppliedText
    Status.SCREENING -> StatusScreeningText
    Status.INTERVIEW -> StatusInterviewText
    Status.OFFER -> StatusOfferText
    Status.REJECTED -> StatusRejectedText
    Status.GHOSTED -> StatusGhostedText
}

fun Status.backgroundColor(): Color = when (this) {
    Status.APPLIED -> StatusAppliedBg
    Status.SCREENING -> StatusScreeningBg
    Status.INTERVIEW -> StatusInterviewBg
    Status.OFFER -> StatusOfferBg
    Status.REJECTED -> StatusRejectedBg
    Status.GHOSTED -> StatusGhostedBg
}
