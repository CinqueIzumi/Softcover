package nl.rhaydus.softcover.core.designsystem.presentation.share

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class ShareCardDimensions(
    val width: Dp,
    val fixedHeight: Dp?,
    val minHeight: Dp,
    val padding: Dp,
) {
    companion object {
        val Default: ShareCardDimensions = ShareCardDimensions(
            width = 360.dp,
            fixedHeight = 540.dp,
            minHeight = 540.dp,
            padding = 28.dp,
        )

        val Book: ShareCardDimensions = ShareCardDimensions(
            width = 420.dp,
            fixedHeight = null,
            minHeight = 480.dp,
            padding = 32.dp,
        )

        val Update: ShareCardDimensions = ShareCardDimensions(
            width = 380.dp,
            fixedHeight = null,
            minHeight = 460.dp,
            padding = 32.dp,
        )

        fun forContent(content: ShareContent): ShareCardDimensions = when (content) {
            is BookShareContent -> Book
            is ReadingUpdateShareContent -> Update
            is StatShareContent,
            is QuoteShareContent,
            is YearRecapShareContent,
                -> Default
        }
    }
}
