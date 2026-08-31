package com.rohit.jobtracker.android.ui.theme

import androidx.compose.ui.graphics.Color
import com.rohit.jobtracker.shared.model.Status

// Brand Theme Colors
val BrandPrimary = Color(0xFF6366F1) // Indigo
val BrandPrimaryContainer = Color(0xFFEEF2FF)
val BrandDarkPrimary = Color(0xFF818CF8)
val BrandDarkPrimaryContainer = Color(0xFF312E81)

// Status Colors (Light & Dark friendly)
val StatusApplied = Color(0xFF0284C7) // Sky Blue
val StatusAppliedBg = Color(0xFFE0F2FE)
val StatusAppliedDarkBg = Color(0xFF075985)

val StatusScreening = Color(0xFFD97706) // Amber
val StatusScreeningBg = Color(0xFFFEF3C7)
val StatusScreeningDarkBg = Color(0xFF78350F)

val StatusInterview = Color(0xFF6366F1) // Indigo
val StatusInterviewBg = Color(0xFFEEF2FF)
val StatusInterviewDarkBg = Color(0xFF312E81)

val StatusOffer = Color(0xFF059669) // Emerald
val StatusOfferBg = Color(0xFFD1FAE5)
val StatusOfferDarkBg = Color(0xFF064E3B)

val StatusRejected = Color(0xFFE11D48) // Rose
val StatusRejectedBg = Color(0xFFFFE4E6)
val StatusRejectedDarkBg = Color(0xFF881337)

val StatusGhosted = Color(0xFF64748B) // Slate
val StatusGhostedBg = Color(0xFFF1F5F9)
val StatusGhostedDarkBg = Color(0xFF1E293B)
val StatusGhostedBorder = Color(0xFF94A3B8)

fun Status.textColor(isDark: Boolean = false): Color = when (this) {
    Status.APPLIED -> if (isDark) Color(0xFF7DD3FC) else StatusApplied
    Status.SCREENING -> if (isDark) Color(0xFFFCD34D) else StatusScreening
    Status.INTERVIEW -> if (isDark) Color(0xFFA5B4FC) else StatusInterview
    Status.OFFER -> if (isDark) Color(0xFF6EE7B7) else StatusOffer
    Status.REJECTED -> if (isDark) Color(0xFFFDA4AF) else StatusRejected
    Status.GHOSTED -> if (isDark) Color(0xFFCBD5E1) else StatusGhosted
}

fun Status.backgroundColor(isDark: Boolean = false): Color = when (this) {
    Status.APPLIED -> if (isDark) StatusAppliedDarkBg.copy(alpha = 0.5f) else StatusAppliedBg
    Status.SCREENING -> if (isDark) StatusScreeningDarkBg.copy(alpha = 0.5f) else StatusScreeningBg
    Status.INTERVIEW -> if (isDark) StatusInterviewDarkBg.copy(alpha = 0.5f) else StatusInterviewBg
    Status.OFFER -> if (isDark) StatusOfferDarkBg.copy(alpha = 0.5f) else StatusOfferBg
    Status.REJECTED -> if (isDark) StatusRejectedDarkBg.copy(alpha = 0.5f) else StatusRejectedBg
    Status.GHOSTED -> if (isDark) StatusGhostedDarkBg.copy(alpha = 0.5f) else StatusGhostedBg
}

// Avatar background colors based on company name hash
val AvatarPalettes = listOf(
    Color(0xFF6366F1), // Indigo
    Color(0xFF0EA5E9), // Sky
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFFEC4899), // Pink
    Color(0xFF8B5CF6), // Purple
    Color(0xFF14B8A6), // Teal
    Color(0xFFF97316)  // Orange
)

fun getCompanyAvatarColor(company: String): Color {
    val hash = kotlin.math.abs(company.hashCode())
    return AvatarPalettes[hash % AvatarPalettes.size]
}
