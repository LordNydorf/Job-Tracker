package com.rohit.jobtracker.android.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.rohit.jobtracker.shared.model.Status

// Brand Identity
val BrandPrimary = Color(0xFF2563EB) // Royal Cobalt Blue
val BrandPrimaryContainer = Color(0xFFDBEAFE)
val BrandDarkPrimary = Color(0xFF60A5FA)
val BrandDarkPrimaryContainer = Color(0xFF1E3A8A)

// High-Contrast Status Colors (WCAG AAA/AA Compliant)
val StatusApplied = Color(0xFF0369A1) // Sky 700
val StatusAppliedDark = Color(0xFF38BDF8)
val StatusAppliedBg = Color(0xFFF0F9FF) // Sky 50
val StatusAppliedDarkBg = Color(0xFF082F49)
val StatusAppliedBorder = Color(0xFFBAE6FD) // Sky 200

val StatusScreening = Color(0xFFB45309) // Amber 700
val StatusScreeningDark = Color(0xFFFBBF24)
val StatusScreeningBg = Color(0xFFFFFBEB) // Amber 50
val StatusScreeningDarkBg = Color(0xFF451A03)
val StatusScreeningBorder = Color(0xFFFDE68A) // Amber 200

val StatusInterview = Color(0xFF4338CA) // Indigo 700
val StatusInterviewDark = Color(0xFF818CF8)
val StatusInterviewBg = Color(0xFFEEF2FF) // Indigo 50
val StatusInterviewDarkBg = Color(0xFF1E1B4B)
val StatusInterviewBorder = Color(0xFFC7D2FE) // Indigo 200

val StatusOffer = Color(0xFF047857) // Emerald 700
val StatusOfferDark = Color(0xFF34D399)
val StatusOfferBg = Color(0xFFECFDF5) // Emerald 50
val StatusOfferDarkBg = Color(0xFF064E3B)
val StatusOfferBorder = Color(0xFFA7F3D0) // Emerald 200

val StatusRejected = Color(0xFFBE123C) // Rose 700
val StatusRejectedDark = Color(0xFFFB7185)
val StatusRejectedBg = Color(0xFFFFF1F2) // Rose 50
val StatusRejectedDarkBg = Color(0xFF4C0519)
val StatusRejectedBorder = Color(0xFFFECDD3) // Rose 200

val StatusGhosted = Color(0xFF475569) // Slate 600
val StatusGhostedDark = Color(0xFF94A3B8)
val StatusGhostedBg = Color(0xFFF1F5F9) // Slate 100
val StatusGhostedDarkBg = Color(0xFF1E293B)
val StatusGhostedBorder = Color(0xFFCBD5E1) // Slate 300
val StatusGhostedDarkBorder = Color(0xFF475569)

fun Status.textColor(isDark: Boolean = false): Color = when (this) {
    Status.APPLIED -> if (isDark) StatusAppliedDark else StatusApplied
    Status.SCREENING -> if (isDark) StatusScreeningDark else StatusScreening
    Status.INTERVIEW -> if (isDark) StatusInterviewDark else StatusInterview
    Status.OFFER -> if (isDark) StatusOfferDark else StatusOffer
    Status.REJECTED -> if (isDark) StatusRejectedDark else StatusRejected
    Status.GHOSTED -> if (isDark) StatusGhostedDark else StatusGhosted
}

fun Status.backgroundColor(isDark: Boolean = false): Color = when (this) {
    Status.APPLIED -> if (isDark) StatusAppliedDarkBg else StatusAppliedBg
    Status.SCREENING -> if (isDark) StatusScreeningDarkBg else StatusScreeningBg
    Status.INTERVIEW -> if (isDark) StatusInterviewDarkBg else StatusInterviewBg
    Status.OFFER -> if (isDark) StatusOfferDarkBg else StatusOfferBg
    Status.REJECTED -> if (isDark) StatusRejectedDarkBg else StatusRejectedBg
    Status.GHOSTED -> if (isDark) StatusGhostedDarkBg else StatusGhostedBg
}

fun Status.borderColor(isDark: Boolean = false): Color = when (this) {
    Status.APPLIED -> if (isDark) StatusAppliedDark.copy(alpha = 0.3f) else StatusAppliedBorder
    Status.SCREENING -> if (isDark) StatusScreeningDark.copy(alpha = 0.3f) else StatusScreeningBorder
    Status.INTERVIEW -> if (isDark) StatusInterviewDark.copy(alpha = 0.3f) else StatusInterviewBorder
    Status.OFFER -> if (isDark) StatusOfferDark.copy(alpha = 0.3f) else StatusOfferBorder
    Status.REJECTED -> if (isDark) StatusRejectedDark.copy(alpha = 0.3f) else StatusRejectedBorder
    Status.GHOSTED -> if (isDark) StatusGhostedDarkBorder else StatusGhostedBorder
}

// 8 Distinct Gradient Palettes for Company Monograms
val AvatarGradients = listOf(
    Pair(Color(0xFF6366F1), Color(0xFF818CF8)), // Indigo
    Pair(Color(0xFF0284C7), Color(0xFF38BDF8)), // Sky
    Pair(Color(0xFF059669), Color(0xFF34D399)), // Emerald
    Pair(Color(0xFFD97706), Color(0xFFFBBF24)), // Amber
    Pair(Color(0xFFDB2777), Color(0xFFF472B6)), // Pink
    Pair(Color(0xFF7C3AED), Color(0xFFA78BFA)), // Purple
    Pair(Color(0xFF0D9488), Color(0xFF2DD4BF)), // Teal
    Pair(Color(0xFFEA580C), Color(0xFFFB923C))  // Orange
)

fun getCompanyAvatarBrush(company: String): Brush {
    val hash = kotlin.math.abs(company.hashCode())
    val pair = AvatarGradients[hash % AvatarGradients.size]
    return Brush.linearGradient(listOf(pair.first, pair.second))
}

fun getCompanyPrimaryColor(company: String): Color {
    val hash = kotlin.math.abs(company.hashCode())
    return AvatarGradients[hash % AvatarGradients.size].first
}
