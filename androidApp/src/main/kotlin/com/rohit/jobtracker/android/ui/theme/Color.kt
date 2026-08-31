package com.rohit.jobtracker.android.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.rohit.jobtracker.shared.model.Status

// Brand Identity
val BrandPrimary = Color(0xFF4F46E5) // Electric Indigo
val BrandPrimaryContainer = Color(0xFFEEF2FF)
val BrandDarkPrimary = Color(0xFF6366F1)
val BrandDarkPrimaryContainer = Color(0xFF312E81)

// High-Contrast Status Colors
val StatusApplied = Color(0xFF0284C7) // Sky
val StatusAppliedDark = Color(0xFF38BDF8)
val StatusAppliedBg = Color(0xFFE0F2FE)
val StatusAppliedDarkBg = Color(0xFF082F49)

val StatusScreening = Color(0xFFD97706) // Amber Gold
val StatusScreeningDark = Color(0xFFFBBF24)
val StatusScreeningBg = Color(0xFFFEF3C7)
val StatusScreeningDarkBg = Color(0xFF451A03)

val StatusInterview = Color(0xFF4F46E5) // Vivid Violet
val StatusInterviewDark = Color(0xFF818CF8)
val StatusInterviewBg = Color(0xFFEEF2FF)
val StatusInterviewDarkBg = Color(0xFF1E1B4B)

val StatusOffer = Color(0xFF059669) // Emerald Spark
val StatusOfferDark = Color(0xFF34D399)
val StatusOfferBg = Color(0xFFD1FAE5)
val StatusOfferDarkBg = Color(0xFF064E3B)

val StatusRejected = Color(0xFFE11D48) // Ruby Crimson
val StatusRejectedDark = Color(0xFFFB7185)
val StatusRejectedBg = Color(0xFFFFE4E6)
val StatusRejectedDarkBg = Color(0xFF4C0519)

val StatusGhosted = Color(0xFF64748B) // Slate Stealth
val StatusGhostedDark = Color(0xFF94A3B8)
val StatusGhostedBg = Color(0xFFF1F5F9)
val StatusGhostedDarkBg = Color(0xFF1E293B)
val StatusGhostedBorder = Color(0xFF94A3B8)

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
