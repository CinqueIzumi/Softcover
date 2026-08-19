package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.designsystem.component.InlineErrorState
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.designsystem.presentation.component.ClickableText
import nl.rhaydus.softcover.core.designsystem.presentation.component.formatLongRelease
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapBlock
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSource
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSpan
import nl.rhaydus.softcover.feature.settings.presentation.action.RefreshRoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.action.RoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState

private val LIST_ITEM_RESTLINE_INDENT = 20.sp
private const val URL_ANNOTATION_TAG = "url"
// region Roadmap content
/**
 * The Roadmap screen body, shared by the mobile [RoadmapScreen] page, the desktop standalone
 * fallback, and the desktop Settings master–detail pane's `Roadmap` category. Renders the parsed
 * public roadmap ([RoadmapDocument]) as an editorial spread rather than raw markdown: headings shaped
 * `## 3.2.0: Title` split into an eyebrow + headline through [EditorialSectionHeader], consecutive
 * bullet/ordered items grouped into one hanging-indent list per run, a hairline [HorizontalBreak] for
 * `---`, and the file's blockquote as a quiet accent-bar aside. [SkeletonCrossfade] carries the
 * ~150ms handoff from [RoadmapUiState.isLoading]'s skeleton to the loaded body; a
 * [RoadmapUiState.roadmapError] renders *above* a document that is already on screen (a
 * stale-but-readable roadmap beats a blank error page) and stands alone only when no document has
 * loaded yet.
 */
@Composable
internal fun RoadmapContent(
    state: RoadmapUiState,
    runAction: (RoadmapAction) -> Unit,
    openUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SkeletonCrossfade(
        isLoading = state.isLoading,
        modifier = modifier.fillMaxWidth(),
    ) { loading ->
        if (loading) {
            RoadmapSkeleton()
        } else {
            RoadmapLoadedBody(
                state = state,
                runAction = runAction,
                openUrl = openUrl,
            )
        }
    }
}

@Composable
private fun RoadmapLoadedBody(
    state: RoadmapUiState,
    runAction: (RoadmapAction) -> Unit,
    openUrl: (String) -> Unit,
) {
    val document = state.document

    Column(modifier = Modifier.fillMaxWidth()) {
        if (document != null) {
            if (state.roadmapError != null) {
                RoadmapErrorBanner(
                    message = state.roadmapError,
                    onRetry = { runAction(RefreshRoadmapAction()) },
                )

                Spacer(modifier = Modifier.height(28.dp))
            }

            RoadmapDocumentBody(
                document = document,
                openUrl = openUrl,
            )
        } else if (state.roadmapError != null) {
            RoadmapErrorBanner(
                message = state.roadmapError,
                onRetry = { runAction(RefreshRoadmapAction()) },
            )
        }
    }
}

@Composable
private fun RoadmapErrorBanner(
    message: String,
    onRetry: () -> Unit,
) {
    InlineErrorState(
        message = message,
        onRetry = onRetry,
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.editorialTypography.bodySmall,
    )
}

@Composable
private fun RoadmapDocumentBody(
    document: RoadmapDocument,
    openUrl: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (document.source == RoadmapSource.BUNDLED) {
            Text(
                text = "Showing the roadmap that shipped with this build — reconnect to see what's " +
                    "changed since.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        val units = groupRoadmapBlocks(blocks = document.blocks)

        units.forEachIndexed { index, unit ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(unit.leadingSpacing()))
            }

            RoadmapUnitContent(
                unit = unit,
                openUrl = openUrl,
            )
        }

        if (document.fetchedAtEpochMillis != null) {
            Spacer(modifier = Modifier.height(32.dp))

            RoadmapLastUpdatedFooter(epochMillis = document.fetchedAtEpochMillis)
        }
    }
}

@Composable
private fun RoadmapLastUpdatedFooter(epochMillis: Long) {
    val dateText = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .formatLongRelease()

    Text(
        text = "Last updated $dateText.",
        style = MaterialTheme.editorialTypography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
// endregion
// region Block grouping
/**
 * One render pass over a [RoadmapDocument]'s flat block list, grouping any consecutive run of
 * [RoadmapBlock.BulletListItem] / [RoadmapBlock.OrderedListItem] into a single [ListGroup] — the
 * domain model emits list items as flat top-level blocks (see [RoadmapBlock]'s KDoc) and leaves this
 * grouping to the renderer.
 */
private sealed interface RoadmapRenderUnit {
    data class Single(val block: RoadmapBlock) : RoadmapRenderUnit

    data class ListGroup(val items: List<RoadmapBlock>) : RoadmapRenderUnit
}

private fun groupRoadmapBlocks(blocks: List<RoadmapBlock>): List<RoadmapRenderUnit> {
    val units = mutableListOf<RoadmapRenderUnit>()
    var index = 0

    while (index < blocks.size) {
        val block = blocks[index]

        if (block is RoadmapBlock.BulletListItem || block is RoadmapBlock.OrderedListItem) {
            val runStart = index

            while (
                index < blocks.size &&
                (blocks[index] is RoadmapBlock.BulletListItem || blocks[index] is RoadmapBlock.OrderedListItem)
            ) {
                index += 1
            }

            val runItems = blocks.subList(
                runStart,
                index,
            )

            units += RoadmapRenderUnit.ListGroup(items = runItems)
        } else {
            units += RoadmapRenderUnit.Single(block = block)
            index += 1
        }
    }

    return units
}

private fun RoadmapRenderUnit.leadingSpacing() = when (this) {
    is RoadmapRenderUnit.ListGroup -> 12.dp

    is RoadmapRenderUnit.Single -> when (block) {
        is RoadmapBlock.Heading -> 32.dp
        is RoadmapBlock.ThematicBreak -> 28.dp
        is RoadmapBlock.BlockQuote -> 20.dp
        is RoadmapBlock.Paragraph -> 16.dp
        is RoadmapBlock.BulletListItem, is RoadmapBlock.OrderedListItem -> 12.dp
    }
}
// endregion
// region Block renderers
@Composable
private fun RoadmapUnitContent(
    unit: RoadmapRenderUnit,
    openUrl: (String) -> Unit,
) {
    when (unit) {
        is RoadmapRenderUnit.ListGroup -> RoadmapList(
            items = unit.items,
            openUrl = openUrl,
        )

        is RoadmapRenderUnit.Single -> when (val block = unit.block) {
            is RoadmapBlock.Heading -> RoadmapHeading(block = block)

            is RoadmapBlock.Paragraph -> RoadmapParagraph(
                spans = block.spans,
                openUrl = openUrl,
            )

            is RoadmapBlock.BlockQuote -> RoadmapBlockQuote(
                spans = block.spans,
                openUrl = openUrl,
            )

            RoadmapBlock.ThematicBreak -> HorizontalBreak()
            is RoadmapBlock.BulletListItem, is RoadmapBlock.OrderedListItem -> Unit
        }
    }
}

/**
 * A release heading shaped `"3.2.0: Widgets, and a lot of small things"` splits on the first `": "`
 * into an eyebrow (the version) + headline (the title) through [EditorialSectionHeader] — the
 * editorial move the shape invites. A heading without that shape (`"Under consideration"`) degrades to
 * a plain headline, since [EditorialSectionHeader] always needs both.
 */
@Composable
private fun RoadmapHeading(block: RoadmapBlock.Heading) {
    val plainText = block.spans.joinToString(separator = "") { it.text }
    val separatorIndex = plainText.indexOf(": ")

    if (separatorIndex > 0 && separatorIndex < plainText.length - 2) {
        EditorialSectionHeader(
            eyebrow = plainText.substring(
                startIndex = 0,
                endIndex = separatorIndex,
            ),
            headline = plainText.substring(startIndex = separatorIndex + 2),
        )
    } else {
        Text(
            text = plainText,
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RoadmapParagraph(
    spans: List<RoadmapSpan>,
    openUrl: (String) -> Unit,
) {
    ClickableText(
        annotatedText = roadmapAnnotatedString(spans = spans),
        style = MaterialTheme.editorialTypography.review.copy(color = MaterialTheme.colorScheme.onSurface),
        handleUrlClick = openUrl,
    )
}

@Composable
private fun RoadmapList(
    items: List<RoadmapBlock>,
    openUrl: (String) -> Unit,
) {
    Column {
        items.forEachIndexed { index, item ->
            val markerAndSpans = when (item) {
                is RoadmapBlock.BulletListItem -> "•" to item.spans
                is RoadmapBlock.OrderedListItem -> "${item.index}." to item.spans
                else -> null
            } ?: return@forEachIndexed

            val (marker, spans) = markerAndSpans

            if (index > 0) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            ClickableText(
                annotatedText = roadmapAnnotatedString(
                    spans = spans,
                    leadingMarker = marker,
                ),
                style = MaterialTheme.editorialTypography.review.copy(color = MaterialTheme.colorScheme.onSurface),
                handleUrlClick = openUrl,
            )
        }
    }
}

/**
 * The file's one blockquote is an aside to the reader, not a citation — a quiet accent bar rather than
 * a boxed card. [IntrinsicSize.Min] lets the bar's `fillMaxHeight` match the text's measured height
 * instead of collapsing to zero, since the [Row] itself has no height of its own to fill.
 */
@Composable
private fun RoadmapBlockQuote(
    spans: List<RoadmapSpan>,
    openUrl: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant),
        )

        Spacer(modifier = Modifier.width(16.dp))

        ClickableText(
            annotatedText = roadmapAnnotatedString(spans = spans),
            style = MaterialTheme.editorialTypography.body.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            handleUrlClick = openUrl,
        )
    }
}

@Composable
private fun HorizontalBreak() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp,
    )
}
// endregion
// region Spans
/**
 * Builds one [AnnotatedString] per text block so the whole run wraps as a single paragraph rather than
 * a `Row` of `Text`s. [leadingMarker] (a bullet `"•"` or an ordered `"N."`) is prepended inside a
 * [ParagraphStyle] carrying a hanging [TextIndent], so a wrapped continuation line aligns under the
 * item's own text rather than under the marker. A span with a non-null [RoadmapSpan.url] is pushed as
 * a `"url"`-tagged string annotation that [ClickableText] resolves back to a tap.
 */
@Composable
private fun roadmapAnnotatedString(
    spans: List<RoadmapSpan>,
    leadingMarker: String? = null,
): AnnotatedString {
    val linkColor = MaterialTheme.colorScheme.primary
    val codeBackground = MaterialTheme.colorScheme.surfaceContainerHigh

    return buildAnnotatedString {
        if (leadingMarker != null) {
            val hangingIndent = TextIndent(
                firstLine = 0.sp,
                restLine = LIST_ITEM_RESTLINE_INDENT,
            )

            pushStyle(style = ParagraphStyle(textIndent = hangingIndent))
            append(leadingMarker)
            append(' ')
        }

        spans.forEach { span ->
            val spanStyle = SpanStyle(
                fontWeight = if (span.bold) FontWeight.Bold else null,
                fontStyle = if (span.italic) FontStyle.Italic else null,
                fontFamily = if (span.code) FontFamily.Monospace else null,
                background = if (span.code) codeBackground else Color.Unspecified,
                color = if (span.url != null) linkColor else Color.Unspecified,
                textDecoration = if (span.url != null) TextDecoration.Underline else null,
            )

            if (span.url != null) {
                pushStringAnnotation(
                    tag = URL_ANNOTATION_TAG,
                    annotation = span.url,
                )

                withStyle(style = spanStyle) { append(span.text) }

                pop()
            } else {
                withStyle(style = spanStyle) { append(span.text) }
            }
        }

        if (leadingMarker != null) pop()
    }
}
// endregion
// region Skeleton
/**
 * The loading placeholder: a version-eyebrow line, a headline line, and a few body lines, shimmering
 * in place of a release section. Kept light — there is always *some* content within the frame — rather
 * than mimicking the document's full variable-length shape.
 */
@Composable
private fun RoadmapSkeleton() {
    Column(modifier = Modifier.fillMaxWidth()) {
        RoadmapSkeletonLine(
            height = 14.dp,
            width = 120.dp,
        )

        Spacer(modifier = Modifier.height(20.dp))

        RoadmapSkeletonLine(
            height = 24.dp,
            width = 220.dp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        RoadmapSkeletonLine(
            height = 16.dp,
            widthFraction = 1f,
        )

        Spacer(modifier = Modifier.height(8.dp))

        RoadmapSkeletonLine(
            height = 16.dp,
            widthFraction = 0.92f,
        )

        Spacer(modifier = Modifier.height(8.dp))

        RoadmapSkeletonLine(
            height = 16.dp,
            widthFraction = 0.6f,
        )
    }
}

@Composable
private fun RoadmapSkeletonLine(
    height: Dp,
    width: Dp? = null,
    widthFraction: Float? = null,
) {
    val widthModifier = when {
        width != null -> Modifier.width(width)
        widthFraction != null -> Modifier.fillMaxWidth(widthFraction)
        else -> Modifier.fillMaxWidth()
    }

    Box(
        modifier = widthModifier
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .shimmer(isLoading = true),
    )
}
// endregion
