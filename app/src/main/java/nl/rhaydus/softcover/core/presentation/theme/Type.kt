package nl.rhaydus.softcover.core.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import nl.rhaydus.softcover.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val frauncesGoogleFont = GoogleFont("Fraunces")
private val interGoogleFont = GoogleFont("Inter")

private val romanWeights = listOf(
    FontWeight.Normal,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold,
)

val displayFontFamily = FontFamily(
    buildList {
        romanWeights.forEach { weight ->
            add(
                Font(
                    googleFont = frauncesGoogleFont,
                    fontProvider = provider,
                    weight = weight,
                    style = FontStyle.Normal,
                ),
            )

            add(
                Font(
                    googleFont = frauncesGoogleFont,
                    fontProvider = provider,
                    weight = weight,
                    style = FontStyle.Italic,
                ),
            )
        }
    },
)

val bodyFontFamily = FontFamily(
    buildList {
        romanWeights.forEach { weight ->
            add(
                Font(
                    googleFont = interGoogleFont,
                    fontProvider = provider,
                    weight = weight,
                    style = FontStyle.Normal,
                ),
            )

            add(
                Font(
                    googleFont = interGoogleFont,
                    fontProvider = provider,
                    weight = weight,
                    style = FontStyle.Italic,
                ),
            )
        }
    },
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = bodyFontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = bodyFontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = bodyFontFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
)
