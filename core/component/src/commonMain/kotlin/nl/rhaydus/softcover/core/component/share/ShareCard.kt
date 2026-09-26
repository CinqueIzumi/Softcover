package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * Renders any user-facing share artefact — book, personal reading update, hero stat, pulled quote,
 * year recap, or whole-history reading-life recap — into a fixed-aspect editorial composition with a
 * trailing `SOFTCOVER` folio sign-off. [content]'s concrete [ShareCardUiModel] subtype selects the
 * variant body and, via [ShareCardDimensions.forContent], the card's fixed size; a variant never
 * overrides those dimensions at the call site.
 *
 * [ShareCard] only draws pixels — it takes no position on exporting them. Pair it with the foundation
 * `CapturableShareCard` + `rememberShareCardCapture(config)` seam (`nl.rhaydus.designsystem.share`) to
 * turn a rendered card into a saved or shared image: wrap this composable in `CapturableShareCard`'s
 * slot, passing [softcoverShareCardCaptureConfig] as the brand config, and drive the returned
 * `ShareCardCapture`'s `saveToGallery(displayName)` / `saveToCache(displayName)` / `share(displayName)`
 * — the last presents the **platform** share sheet (an Android `ACTION_SEND` chooser, an iOS
 * `UIActivityViewController`, or a desktop clipboard copy) rather than the caller building one. A
 * caller that just wants to share the card calls `capture.share(...)` and branches on the returned
 * `ShareOutcome` (`Shared` / `Cancelled` / `Failure`, with a dismissed share sheet reported as
 * `Cancelled` — a no-op, not a failure) instead of constructing an intent itself. Legacy gallery-write
 * permission (API 26–28) goes through `rememberGalleryWritePermissionRequester`, a synchronous
 * pass-through from API 29 on; the iOS gallery save is not yet wired.
 */
@Composable
fun ShareCard(
    content: ShareCardUiModel,
    modifier: Modifier = Modifier,
) {
    val surfaceColor = when (content) {
        is StatShareCardUiModel,
        is ReadingLifeShareCardUiModel -> MaterialTheme.colorScheme.primary
        is BookShareCardUiModel,
        is ReadingUpdateShareCardUiModel,
        is QuoteShareCardUiModel,
        is YearRecapShareCardUiModel -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when (content) {
        is StatShareCardUiModel,
        is ReadingLifeShareCardUiModel -> MaterialTheme.colorScheme.onPrimary
        is BookShareCardUiModel,
        is ReadingUpdateShareCardUiModel,
        is QuoteShareCardUiModel,
        is YearRecapShareCardUiModel -> MaterialTheme.colorScheme.onSurface
    }

    val dimensions = ShareCardDimensions.forContent(content = content)

    val sizeModifier = if (dimensions.fixedHeight != null) {
        Modifier.requiredSize(
            width = dimensions.width,
            height = dimensions.fixedHeight,
        )
    } else {
        Modifier
            .requiredWidth(width = dimensions.width)
            .requiredHeightIn(min = dimensions.minHeight)
    }

    Surface(
        modifier = modifier
            .then(other = sizeModifier)
            .clip(RoundedCornerShape(20.dp)),
        color = surfaceColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensions.padding),
        ) {
            when (content) {
                is BookShareCardUiModel -> BookShareCardBody(content)
                is ReadingUpdateShareCardUiModel -> ReadingUpdateShareCardBody(content)
                is StatShareCardUiModel -> StatShareCardBody(content)
                is QuoteShareCardUiModel -> QuoteShareCardBody(content)
                is YearRecapShareCardUiModel -> YearRecapShareCardBody(content)
                is ReadingLifeShareCardUiModel -> ReadingLifeShareCardBody(content)
            }

            Spacer(modifier = Modifier.weight(1f))

            ShareCardSignOff()
        }
    }
}

@Composable
private fun ShareCardSignOff() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "SOFTCOVER",
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = LocalContentColor.current.copy(alpha = 0.6f),
        )

        Text(
            text = "— · —",
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = LocalContentColor.current.copy(alpha = 0.4f),
        )
    }
}
