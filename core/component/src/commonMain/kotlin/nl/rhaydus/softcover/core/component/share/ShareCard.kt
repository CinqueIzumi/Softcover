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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * Renders any user-facing share artefact — book, personal reading update, hero stat, pulled quote,
 * year recap, or whole-history reading-life recap — into a fixed-aspect editorial composition with a
 * trailing `SOFTCOVER` sign-off. [model]'s concrete [ShareCardUiModel] subtype selects the
 * variant body and the card's size; a variant never overrides those dimensions at the call site.
 *
 * [ShareCard] only draws pixels — it takes no position on exporting them. Show it through
 * [ScaledShareCardPreview] with a [ShareCardCapture] from [rememberShareCardCapture], passing
 * [softcoverShareCardCaptureConfig] as the brand config, and drive the capture's
 * `saveToGallery(displayName)` / `saveToCache(displayName)` / `share(displayName)`. The last presents
 * the **platform** share sheet (an Android `ACTION_SEND` chooser or an iOS `UIActivityViewController`),
 * or copies to the clipboard on desktop, rather than the caller building one. Branch on the returned
 * [ShareOutcome] and [SaveOutcome]: a dismissed share sheet or save dialog is `Cancelled`, a no-op and
 * not a failure. Legacy gallery-write permission (API 26–28) goes through the foundation's
 * `rememberGalleryWritePermissionRequester`, a synchronous pass-through from API 29 on; the iOS gallery
 * save is not yet wired.
 */
@Composable
fun ShareCard(
    model: ShareCardUiModel,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    when (model) {
        is BookShareCardUiModel -> BookShareCardFrame(
            eyebrow = "FROM THE SHELF",
            cover = model.cover,
            title = model.title,
            author = model.author,
            username = null,
            avatarUrl = null,
            format = model.format,
            modifier = modifier,
        ) {
            BookShareCardBody(model)
        }

        is ReadingUpdateShareCardUiModel -> BookShareCardFrame(
            eyebrow = when (model) {
                is ReadingUpdateShareCardUiModel.Finished -> "FROM MY SHELF"
                is ReadingUpdateShareCardUiModel.Reading -> "CURRENTLY READING"
            },
            cover = model.cover,
            title = model.title,
            author = model.author,
            username = model.username,
            avatarUrl = model.avatarUrl,
            format = model.format,
            modifier = modifier,
        ) {
            ReadingUpdateShareCardBody(model)
        }

        is StatShareCardUiModel -> PaperShareCard(
            surfaceColor = colors.primary,
            contentColor = colors.onPrimary,
            sizeModifier = Modifier.requiredSize(
                width = 360.dp,
                height = 540.dp,
            ),
            contentPadding = Spacing.xxl,
            modifier = modifier,
        ) {
            StatShareCardBody(model)
        }

        is QuoteShareCardUiModel -> PaperShareCard(
            surfaceColor = colors.surface,
            contentColor = colors.onSurface,
            sizeModifier = Modifier.requiredSize(
                width = 360.dp,
                height = 540.dp,
            ),
            contentPadding = Spacing.xxl,
            modifier = modifier,
        ) {
            QuoteShareCardBody(model)
        }

        is YearRecapShareCardUiModel -> PaperShareCard(
            surfaceColor = colors.surface,
            contentColor = colors.onSurface,
            sizeModifier = Modifier.requiredSize(
                width = 360.dp,
                height = 540.dp,
            ),
            contentPadding = Spacing.xxl,
            modifier = modifier,
        ) {
            YearRecapShareCardBody(model)
        }

        is ReadingLifeShareCardUiModel -> PaperShareCard(
            surfaceColor = colors.primary,
            contentColor = colors.onPrimary,
            sizeModifier = Modifier
                .requiredWidth(300.dp)
                .requiredHeightIn(min = 560.dp),
            contentPadding = Spacing.xl,
            modifier = modifier,
        ) {
            ReadingLifeShareCardBody(model)
        }
    }
}

@Composable
private fun PaperShareCard(
    surfaceColor: Color,
    contentColor: Color,
    sizeModifier: Modifier,
    contentPadding: Dp,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
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
            content()

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
