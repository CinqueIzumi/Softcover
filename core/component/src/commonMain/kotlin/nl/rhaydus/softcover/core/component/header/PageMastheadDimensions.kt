package nl.rhaydus.softcover.core.component.header

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.EditorialTypography

internal val SUBTITLE_MAX_WIDTH = 300.dp

/** Per-[PageMastheadSize] treatment: title/subtitle type roles and the title-to-subtitle gap. */
internal data class PageMastheadDimensions(
    val titleStyle: TextStyle,
    val subtitleStyle: TextStyle,
    val titleToSubtitleGap: Dp,
) {
    companion object {
        fun forSize(
            size: PageMastheadSize,
            typography: EditorialTypography,
        ): PageMastheadDimensions =
            when (size) {
                PageMastheadSize.Regular -> PageMastheadDimensions(
                    titleStyle = typography.pageTitle,
                    subtitleStyle = typography.body,
                    titleToSubtitleGap = 6.dp,
                )

                PageMastheadSize.Compact -> PageMastheadDimensions(
                    titleStyle = typography.headlineMedium,
                    subtitleStyle = typography.bodySmall,
                    titleToSubtitleGap = 4.dp,
                )
            }
    }
}
