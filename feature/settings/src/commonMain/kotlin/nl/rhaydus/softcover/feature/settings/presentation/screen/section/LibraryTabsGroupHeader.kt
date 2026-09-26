package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

private const val LIST_DOT_INLINE_CONTENT_ID = "library-tabs-list-dot"

/**
 * The group-level opener above the row list: a small primary eyebrow and an italic Fraunces subhead —
 * deliberately bar-less (§2.3's no-bar register), since
 * [EditorialSectionHeader][nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader] already
 * opened the screen once above and a second accent bar here would be group-level ceremony the redesign
 * retires.
 */
@Composable
internal fun LibraryTabsGroupHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Your tabs".uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(letterSpacing = 2.2.sp),
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(6.dp))

        LibraryTabsGroupSubhead()
    }
}

/**
 * The group subhead sentence, with the "a list" legend's 6dp primary dot embedded inline via
 * [InlineTextContent] so it sits mid-sentence rather than as a separate leading glyph.
 */
@Composable
private fun LibraryTabsGroupSubhead() {
    val dotColor = MaterialTheme.colorScheme.primary
    val dotSizeSp = with(LocalDensity.current) { 6.dp.toSp() }

    val annotatedText = buildAnnotatedString {
        append("Drag to reorder — shelves and lists sit in one line. A ")
        appendInlineContent(
            id = LIST_DOT_INLINE_CONTENT_ID,
            alternateText = "•",
        )
        append(" marks a list.")
    }

    Text(
        text = annotatedText,
        style = MaterialTheme.editorialTypography.body.copy(
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        inlineContent = mapOf(
            LIST_DOT_INLINE_CONTENT_ID to InlineTextContent(
                placeholder = Placeholder(
                    width = dotSizeSp,
                    height = dotSizeSp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(dotColor),
                )
            },
        ),
    )
}
