package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The canonical sheet opening — accent bar, eyebrow, italic headline, italic description — composed
 * locally rather than through `EditorialSectionHeader`, which has no trailing slot for the book's mini
 * jacket and no way to tint a substring of its description. It shares that component's anatomy and type
 * roles, with spacing tuned to the redline rather than the generic component's defaults; the layout (a
 * trailing cover) and the mixed-color description are bespoke.
 */
@Composable
internal fun TagEditorHeader(
    bookTitle: String,
    cover: CoverUiModel?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "YOUR TAGS",
                    style = MaterialTheme.editorialTypography.eyebrow,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tag it your way.",
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tagEditorDescription(bookTitle = bookTitle),
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Cover(
            model = cover,
            modifier = Modifier.width(52.dp),
        )
    }
}

@Composable
private fun tagEditorDescription(bookTitle: String): AnnotatedString {
    val titleColor = MaterialTheme.colorScheme.primary

    return remember(bookTitle, titleColor) {
        buildAnnotatedString {
            append("Your own genres, moods and notes on ")
            withStyle(SpanStyle(color = titleColor)) {
                append(bookTitle)
            }
            append(". Mark any as a spoiler to keep it off shared cards.")
        }
    }
}
