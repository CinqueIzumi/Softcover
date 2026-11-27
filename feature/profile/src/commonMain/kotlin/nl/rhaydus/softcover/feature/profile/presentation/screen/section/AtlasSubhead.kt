package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.text.FadeThroughText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * A section's [title] over its [caption] inside a chapter. Both fade between scopes, and while [isLoading]
 * each is masked at the length of the text it stands in for. A blank [caption] drops its
 * line and the gap above it, easing the height.
 */
@Composable
internal fun AtlasSubhead(
    title: String,
    caption: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.animateContentSize()) {
        FadeThroughText(
            text = title,
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            isLoading = isLoading,
            standIn = title,
        )

        if (caption.isNotBlank()) {
            Spacer(modifier = Modifier.height(Spacing.xxs))

            FadeThroughText(
                text = caption,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                isLoading = isLoading,
                standIn = caption,
            )
        }
    }
}
