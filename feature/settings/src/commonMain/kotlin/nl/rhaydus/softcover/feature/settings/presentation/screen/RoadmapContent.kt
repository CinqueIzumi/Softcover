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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.designsystem.component.InlineErrorState
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapBlock
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSource
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSpan
import nl.rhaydus.softcover.feature.settings.presentation.action.RefreshRoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.action.RoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.model.RoadmapRenderUnit
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
// region Roadmap content
/**
 * The Roadmap screen body, shared by the mobile [RoadmapScreen] page, the desktop standalone
 * fallback, and the desktop Settings master–detail pane's `Roadmap` category. Renders the parsed
 * public roadmap ([RoadmapDocument]) as an editorial spread rather than raw markdown, from
 * [RoadmapUiState.renderUnits]: headings shaped `## 3.2.0: Title` split into an eyebrow + headline
 * through [SectionHeader], consecutive bullet/ordered items grouped into one hanging-indent list per
 * run, a hairline [Divider] for `---`, and the file's blockquote as a quiet accent-bar aside. A
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
    val document = state.document

    Column(modifier = modifier.fillMaxWidth()) {
        if (document != null || state.isLoading) {
            if (state.roadmapError != null) {
                RoadmapErrorBanner(
                    message = state.roadmapError,
                    onRetry = { runAction(RefreshRoadmapAction()) },
                )

                Spacer(modifier = Modifier.height(Spacing.xxl))
            }

            RoadmapDocumentBody(
                isBundled = document?.source == RoadmapSource.BUNDLED,
                renderUnits = state.renderUnits,
                lastUpdatedText = state.lastUpdatedText,
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
    isBundled: Boolean,
    renderUnits: ImmutableList<RoadmapRenderUnit>,
    lastUpdatedText: String?,
    openUrl: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (isBundled) {
            SoftcoverText(
                text = "This is the roadmap that came with this version. Go online to see " +
                    "what's changed.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
        }

        renderUnits.forEachIndexed { index, unit ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(unit.leadingSpacing()))
            }

            RoadmapUnitContent(
                unit = unit,
                openUrl = openUrl,
            )
        }

        if (lastUpdatedText != null) {
            Spacer(modifier = Modifier.height(Spacing.xxxl))

            RoadmapLastUpdatedFooter(text = lastUpdatedText)
        }
    }
}

@Composable
private fun RoadmapLastUpdatedFooter(text: String) {
    SoftcoverText(
        text = "Last updated $text.",
        style = MaterialTheme.editorialTypography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
// endregion
// region Block grouping
private fun RoadmapRenderUnit.leadingSpacing() = when (this) {
    is RoadmapRenderUnit.ListGroup -> Spacing.sm
    is RoadmapRenderUnit.Heading, is RoadmapRenderUnit.PlainHeading -> Spacing.section
    is RoadmapRenderUnit.ThematicBreak -> Spacing.xxl
    is RoadmapRenderUnit.BlockQuote -> Spacing.lg
    is RoadmapRenderUnit.Paragraph -> Spacing.md
}
// endregion
// region Block renderers
@Composable
private fun RoadmapUnitContent(
    unit: RoadmapRenderUnit,
    openUrl: (String) -> Unit,
) {
    when (unit) {
        is RoadmapRenderUnit.Heading -> SectionHeader(model = unit.header)

        is RoadmapRenderUnit.PlainHeading -> SoftcoverText(
            text = unit.text,
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        is RoadmapRenderUnit.ListGroup -> RoadmapList(
            items = unit.items,
            openUrl = openUrl,
            isLoading = unit.isLoading,
        )

        is RoadmapRenderUnit.Paragraph -> RoadmapParagraph(
            spans = unit.spans,
            openUrl = openUrl,
        )

        is RoadmapRenderUnit.BlockQuote -> RoadmapBlockQuote(
            spans = unit.spans,
            openUrl = openUrl,
        )

        RoadmapRenderUnit.ThematicBreak -> Divider()
    }
}

@Composable
private fun RoadmapParagraph(
    spans: ImmutableList<RoadmapSpan>,
    openUrl: (String) -> Unit,
) {
    SoftcoverText(
        text = roadmapAnnotatedString(
            spans = spans,
            openUrl = openUrl,
        ),
        style = MaterialTheme.editorialTypography.review.copy(color = MaterialTheme.colorScheme.onSurface),
    )
}

@Composable
private fun RoadmapList(
    items: ImmutableList<RoadmapBlock>,
    openUrl: (String) -> Unit,
    isLoading: Boolean = false,
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
                Spacer(modifier = Modifier.height(Spacing.xs))
            }

            SoftcoverText(
                text = roadmapAnnotatedString(
                    spans = spans,
                    openUrl = openUrl,
                    leadingMarker = marker,
                ),
                modifier = Modifier.skeletonMask(visible = isLoading),
                style = MaterialTheme.editorialTypography.review.copy(color = MaterialTheme.colorScheme.onSurface),
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
    spans: ImmutableList<RoadmapSpan>,
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

        Spacer(modifier = Modifier.width(Spacing.md))

        SoftcoverText(
            text = roadmapAnnotatedString(
                spans = spans,
                openUrl = openUrl,
            ),
            style = MaterialTheme.editorialTypography.body.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        )
    }
}
// endregion
// region Spans
/**
 * Builds one [AnnotatedString] per text block so the whole run wraps as a single paragraph rather than
 * a `Row` of `Text`s. [leadingMarker] (a bullet `"•"` or an ordered `"N."`) is prepended inside a
 * [ParagraphStyle] carrying a hanging [TextIndent], so a wrapped continuation line aligns under the
 * item's own text rather than under the marker. A span with a non-null [RoadmapSpan.url] is pushed as
 * a [LinkAnnotation.Url] whose tap is handed to [openUrl].
 */
@Composable
private fun roadmapAnnotatedString(
    spans: ImmutableList<RoadmapSpan>,
    openUrl: (String) -> Unit,
    leadingMarker: String? = null,
): AnnotatedString {
    val linkColor = MaterialTheme.colorScheme.primary
    val codeBackground = MaterialTheme.colorScheme.surfaceContainerHigh

    return buildAnnotatedString {
        if (leadingMarker != null) {
            val hangingIndent = TextIndent(
                firstLine = 0.sp,
                restLine = 20.sp,
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
                val link = LinkAnnotation.Url(
                    url = span.url,
                    linkInteractionListener = { openUrl(span.url) },
                )

                withLink(link = link) {
                    withStyle(style = spanStyle) { append(span.text) }
                }
            } else {
                withStyle(style = spanStyle) { append(span.text) }
            }
        }

        if (leadingMarker != null) pop()
    }
}
// endregion
