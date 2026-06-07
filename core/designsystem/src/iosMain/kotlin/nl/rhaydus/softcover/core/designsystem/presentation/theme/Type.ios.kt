package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.ui.text.font.FontFamily

// TODO(iOS): ship the branded Fraunces (display) and Inter (body) fonts — bundle the TTFs under
//  commonMain/composeResources/font and build FontFamily(Font(Res.font.…)). Until then iOS renders
//  with the system family, so the editorial type voice is not yet correct on iOS.
actual val displayFontFamily: FontFamily = FontFamily.Default

actual val bodyFontFamily: FontFamily = FontFamily.Default
