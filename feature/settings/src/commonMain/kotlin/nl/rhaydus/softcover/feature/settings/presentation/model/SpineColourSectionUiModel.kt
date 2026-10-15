package nl.rhaydus.softcover.feature.settings.presentation.model

import androidx.compose.runtime.Immutable

/** The spine-colour section's [gloss] line and whether its tiles can be tapped ([interactive]). */
@Immutable
internal data class SpineColourSectionUiModel(
    val gloss: String,
    val interactive: Boolean,
)
