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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * Renders any user-facing share artefact — book, personal reading update, hero stat, pulled quote,
 * year recap, or whole-history reading-life recap — into a fixed-aspect editorial composition with a
 * trailing `SOFTCOVER` folio sign-off. [model]'s concrete [ShareCardUiModel] subtype selects the
 * variant body and the card's size; a variant never overrides those dimensions at the call site.
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
    model: ShareCardUiModel,
    modifier: Modifier = Modifier,
) {
    val surfaceColor = when (model) {
        is StatShareCardUiModel,
        is ReadingLifeShareCardUiModel -> MaterialTheme.colorScheme.primary
        is BookShareCardUiModel,
        is ReadingUpdateShareCardUiModel,
        is QuoteShareCardUiModel,
        is YearRecapShareCardUiModel -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when (model) {
        is StatShareCardUiModel,
        is ReadingLifeShareCardUiModel -> MaterialTheme.colorScheme.onPrimary
        is BookShareCardUiModel,
        is ReadingUpdateShareCardUiModel,
        is QuoteShareCardUiModel,
        is YearRecapShareCardUiModel -> MaterialTheme.colorScheme.onSurface
    }

    val sizeModifier = when (model) {
        is BookShareCardUiModel -> Modifier
            .requiredWidth(420.dp)
            .requiredHeightIn(min = 480.dp)

        is ReadingUpdateShareCardUiModel -> Modifier
            .requiredWidth(380.dp)
            .requiredHeightIn(min = 460.dp)

        is ReadingLifeShareCardUiModel -> Modifier
            .requiredWidth(300.dp)
            .requiredHeightIn(min = 560.dp)

        is StatShareCardUiModel,
        is QuoteShareCardUiModel,
        is YearRecapShareCardUiModel -> Modifier.requiredSize(
            width = 360.dp,
            height = 540.dp,
        )
    }

    val contentPadding = when (model) {
        is BookShareCardUiModel,
        is ReadingUpdateShareCardUiModel -> Spacing.xxxl
        is ReadingLifeShareCardUiModel -> Spacing.xl
        is StatShareCardUiModel,
        is QuoteShareCardUiModel,
        is YearRecapShareCardUiModel -> Spacing.xxl
    }

    Surface(
        modifier = modifier
            .then(other = sizeModifier)
            .clip(Corners.xl),
        color = surfaceColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            when (model) {
                is BookShareCardUiModel -> BookShareCardBody(model)
                is ReadingUpdateShareCardUiModel -> ReadingUpdateShareCardBody(model)
                is StatShareCardUiModel -> StatShareCardBody(model)
                is QuoteShareCardUiModel -> QuoteShareCardBody(model)
                is YearRecapShareCardUiModel -> YearRecapShareCardBody(model)
                is ReadingLifeShareCardUiModel -> ReadingLifeShareCardBody(model)
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
        SoftcoverText(
            text = "SOFTCOVER",
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = LocalContentColor.current.copy(alpha = 0.6f),
        )

        SoftcoverText(
            text = "— · —",
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = LocalContentColor.current.copy(alpha = 0.4f),
        )
    }
}
