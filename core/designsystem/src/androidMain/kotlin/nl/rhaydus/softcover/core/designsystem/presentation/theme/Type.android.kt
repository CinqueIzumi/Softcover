package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import nl.rhaydus.softcover.core.designsystem.R

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

actual val displayFontFamily = FontFamily(
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

actual val bodyFontFamily = FontFamily(
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
