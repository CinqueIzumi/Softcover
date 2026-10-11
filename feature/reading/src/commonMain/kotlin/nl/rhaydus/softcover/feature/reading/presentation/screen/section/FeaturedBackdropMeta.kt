package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.component.state.RevealedWhenPresent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.reading.presentation.state.FeaturedBookUiModel

/** The series eyebrow, hero title, byline, and "Your pace" row over the hero backdrop. */
@Composable
internal fun FeaturedBackdropMeta(
    book: FeaturedBookUiModel,
    mutationFailed: Boolean,
    paceForecast: String?,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val overlayShadow = Shadow(
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
        offset = Offset(
            x = 0f,
            y = 1f,
        ),
        blurRadius = 14f,
    )
    val foreground = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier) {
        val topLineText = if (mutationFailed) {
            "Couldn't save — tap to retry".uppercase()
        } else {
            book.seriesText?.takeIf { it.isNotBlank() }?.uppercase()
        }

        if (topLineText != null) {
            SoftcoverText(
                text = topLineText,
                isLoading = isLoading,
                standIn = topLineText,
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                    letterSpacing = 1.3.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = overlayShadow,
                ),
                color = if (mutationFailed) MaterialTheme.colorScheme.error else foreground.copy(alpha = 0.86f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(Spacing.xs))
        }

        SoftcoverText(
            text = book.title,
            isLoading = isLoading,
            standIn = book.title,
            style = MaterialTheme.editorialTypography.headlineSmall.copy(
                lineHeight = 27.sp,
                shadow = overlayShadow,
            ),
            color = foreground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        book.authors?.takeIf { it.isNotBlank() }?.let { authors ->
            Spacer(modifier = Modifier.height(Spacing.xs))

            SoftcoverText(
                text = "By $authors".uppercase(),
                isLoading = isLoading,
                standIn = "By $authors".uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                    letterSpacing = 1.2.sp,
                    shadow = overlayShadow,
                ),
                color = foreground.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        RevealedWhenPresent(value = paceForecast) { paceLine ->
            Column {
                Spacer(modifier = Modifier.height(Spacing.md))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SoftcoverText(
                        text = "Your pace".uppercase(),
                        style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                            letterSpacing = 1.4.sp,
                            shadow = overlayShadow,
                        ),
                        color = foreground.copy(alpha = 0.8f),
                        maxLines = 1,
                    )

                    Spacer(modifier = Modifier.width(Spacing.xs))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(foreground.copy(alpha = 0.24f)),
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                SoftcoverText(
                    text = paceLine,
                    style = MaterialTheme.editorialTypography.bodySmall.copy(shadow = overlayShadow),
                    color = foreground.copy(alpha = 0.95f),
                )
            }
        }
    }
}
