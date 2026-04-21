package com.verbum.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.verbum.core.ui.R

// Google Fonts provider (kept for Cinzel headings & fallback)
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// ── Heading font: Cinzel (Roman-inspired serif — used for display/headline) ──
private val cinzelFont = GoogleFont("Cinzel")
val CinzelFamily = FontFamily(
    Font(googleFont = cinzelFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = cinzelFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = cinzelFont, fontProvider = provider, weight = FontWeight.Bold),
)

// ── Default app font: EB Garamond (elegant serif — body, title, label) ──
val EBGaramondFamily = FontFamily(
    Font(R.font.eb_garamond_regular, FontWeight.Normal),
    Font(R.font.eb_garamond_medium, FontWeight.Medium),
    Font(R.font.eb_garamond_semibold, FontWeight.SemiBold),
    Font(R.font.eb_garamond_bold, FontWeight.Bold),
    Font(R.font.eb_garamond_italic, FontWeight.Normal, FontStyle.Italic),
)

// ── Classic theme titles: Trajan Pro (Roman inscriptions) ──
val TrajanProFamily = FontFamily(
    Font(R.font.trajan_pro_regular, FontWeight.Normal),
    Font(R.font.trajan_pro_bold, FontWeight.Bold),
)

// ── Manuscript theme titles: UnifrakturCook (Gothic blackletter) ──
val UnifrakturCookFamily = FontFamily(
    Font(R.font.unifrakturcook_bold, FontWeight.Bold),
)

// ── Manuscript theme body: Cormorant Garamond (old-world elegance) ──
val CormorantGaramondFamily = FontFamily(
    Font(R.font.cormorant_garamond_regular, FontWeight.Normal),
    Font(R.font.cormorant_garamond_medium, FontWeight.Medium),
    Font(R.font.cormorant_garamond_semibold, FontWeight.SemiBold),
    Font(R.font.cormorant_garamond_bold, FontWeight.Bold),
)

// ── Legacy: Inter (clean modern sans-serif — kept for UI elements) ──
private val interFont = GoogleFont("Inter")
val InterFamily = FontFamily(
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Bold),
)

// ── Scripture font: Crimson Text (reverent serif — verse display) ──
private val crimsonFont = GoogleFont("Crimson Text")
val CrimsonTextFamily = FontFamily(
    Font(googleFont = crimsonFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = crimsonFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = crimsonFont, fontProvider = provider, weight = FontWeight.Bold),
)

// ═══════════════════════════════════════════════════════════
// VERBUM TYPOGRAPHY SYSTEM — EB Garamond default
// ═══════════════════════════════════════════════════════════
val VerbumTypography = Typography(

    // ── Display (Cinzel for grandeur) ──
    displayLarge = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),

    // ── Headline (Cinzel for section/screen titles) ──
    headlineLarge = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),

    // ── Title (EB Garamond) ──
    titleLarge = TextStyle(
        fontFamily = EBGaramondFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = EBGaramondFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = EBGaramondFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // ── Body (EB Garamond) ──
    bodyLarge = TextStyle(
        fontFamily = EBGaramondFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = EBGaramondFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = EBGaramondFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // ── Label (EB Garamond) ──
    labelLarge = TextStyle(
        fontFamily = EBGaramondFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = EBGaramondFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = EBGaramondFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ── Scripture-specific text styles ──
object ScriptureTypography {
    val verseText = TextStyle(
        fontFamily = EBGaramondFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.15.sp
    )
    val verseNumber = TextStyle(
        fontFamily = EBGaramondFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    val bookTitle = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    )
    val chapterTitle = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
}
