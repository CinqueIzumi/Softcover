package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import nl.rhaydus.common.secondsToHm
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book

/**
 * "Page {x} of {y}" (or the audiobook time equivalent) beside the "NN%" stat, then a wavy bar — set
 * in [foreground] (rather than a `onSurface`/`onSurfaceVariant` theme pair) since this now sits over
 * the featured card's single full-card blurred-cover backdrop, not a flat surface below it.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FeaturedProgressStat(
    book: Book,
    foreground: Color,
) {
    val progressFraction = (book.userBookRead?.progress ?: 0f) / 100f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = pageCountLabel(
                book = book,
                foreground = foreground,
            ),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = foreground,
        )

        Text(
            text = "${(book.userBookRead?.progress ?: 0f).roundToInt()}%",
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = foreground,
        )
    }

    Spacer(modifier = Modifier.height(9.dp))

    LinearWavyProgressIndicator(
        progress = {
            progressFraction.coerceIn(
                0f,
                1f,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp),
    )
}

/** "Page 348" at full [foreground] alpha followed by " of 512" demoted to 0.6 alpha, in one line. */
@Composable
private fun pageCountLabel(
    book: Book,
    foreground: Color,
): AnnotatedString {
    val edition = book.currentEdition
    val dimColor = foreground.copy(alpha = 0.6f)

    return buildAnnotatedString {
        if (edition?.isAudiobook == true) {
            val current = book.userBookRead?.currentSeconds ?: 0
            val total = edition.audioSeconds ?: 0

            append(secondsToHm(current))
            withStyle(SpanStyle(color = dimColor)) {
                append(" of ${secondsToHm(total)}")
            }
        } else {
            val currentPage = book.userBookRead?.currentPage ?: 0
            val totalPages = edition?.pages ?: book.defaultEdition?.pages

            append("Page $currentPage")
            withStyle(SpanStyle(color = dimColor)) {
                append(" of $totalPages")
            }
        }
    }
}
