package nl.rhaydus.softcover.feature.session.presentation.screen

import nl.rhaydus.common.currentInstant
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSuffix
import nl.rhaydus.designsystem.editorial.component.HeroStatNumberField
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.designsystem.presentation.session.formatSessionElapsed
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The distraction-free reading hero shared by both platforms: eyebrow → cover → title/author → the
 * running timer as a `statHero` hero stat → the editable page field → progress → pause/resume + stop.
 * Owns the 1s ticking [currentInstant] state so the timer advances identically on mobile and desktop.
 * The caller supplies the [Column] modifier (insets / scroll / measure) and the [coverWidth] (mobile
 * stays compact; desktop renders a larger cover). Hover/cursor on the buttons is inert on touch, so
 * mobile renders identically.
 */
@Composable
internal fun FocusReadingPanel(
    active: ActiveSession,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onUpdatePage: (Int) -> Unit,
    coverWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberHaptics()
    val isPaused = active.session.isPaused

    var now by remember { mutableStateOf(currentInstant()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = currentInstant()

            delay(timeMillis = 1_000)
        }
    }

    val elapsed = active.session.readingDuration(now = now)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            text = "NOW READING",
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(14.dp))

        EditionImage(
            edition = active.book.currentEdition,
            defaultEdition = active.book.defaultEdition,
            isLoading = false,
            fallbackCoverUrl = active.book.coverUrl,
            elevation = 20.dp,
            cornerRadius = 8.dp,
            shadowColor = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier
                .width(coverWidth)
                .aspectRatio(2f / 3f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = active.book.title,
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        active.book.authors.firstOrNull()?.let { author ->
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "By ${author.name}",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = if (isPaused) "PAUSED" else "READING TIME",
            style = MaterialTheme.editorialTypography.eyebrow,
            color = if (isPaused) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            },
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = formatSessionElapsed(elapsed = elapsed),
            style = MaterialTheme.editorialTypography.statHero,
            color = if (isPaused) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )

        Spacer(modifier = Modifier.height(28.dp))

        FocusPageEditor(
            active = active,
            onCommit = { page ->
                haptics.commit()

                onUpdatePage(page)
            },
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RhaydusButton(
                label = if (isPaused) "Resume" else "Pause",
                style = ButtonStyle.FILLED,
                size = ButtonSize.M,
                icon = drawableIconResource(
                    icon = if (isPaused) SoftcoverIcon.Play else SoftcoverIcon.Pause,
                    contentDescription = "",
                ),
                onClick = {
                    haptics.select()

                    onPauseResume()
                },
                modifier = Modifier
                    .weight(1f)
                    .pointerHandCursor(),
            )

            RhaydusButton(
                label = "Stop",
                style = ButtonStyle.OUTLINED,
                size = ButtonSize.M,
                icon = drawableIconResource(
                    icon = SoftcoverIcon.Stop,
                    contentDescription = "",
                ),
                onClick = {
                    haptics.threshold()

                    onStop()
                },
                modifier = Modifier
                    .weight(1f)
                    .pointerHandCursor(),
            )
        }
    }
}

/**
 * The reading position rendered as an editable hero stat (DS §5) — the same treatment as the
 * progress sheet's page tab: a borderless [HeroStatNumberField] in the stat face over an editorial
 * "of N pages" caption and a progress bar. Tapping it opens the keyboard; the new page commits when
 * the field loses focus (keyboard "Done" or tapping away), and only when it actually changed. Books
 * without a page count (audiobooks / unknown) fall back to a read-only "% complete" line.
 */
@Composable
private fun FocusPageEditor(
    active: ActiveSession,
    onCommit: (Int) -> Unit,
) {
    val percent = active.book.userBookRead?.progress ?: 0f
    val totalPages = active.book.currentEdition?.pages ?: active.book.defaultEdition?.pages ?: 0

    if (totalPages <= 0) {
        EditorialSuffix(text = "${percent.roundToInt()}% complete")

        Spacer(modifier = Modifier.height(12.dp))

        FocusProgressBar(fraction = percent / 100f)

        return
    }

    val initialPage = active.book.userBookRead?.currentPage ?: 0

    var number by remember { mutableStateOf(TextFieldValue(text = initialPage.toString())) }

    var firstTimeFocusGained by remember { mutableStateOf(true) }

    val parsed = number.text.toIntOrNull() ?: 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        HeroStatNumberField(
            value = number,
            charCount = 4,
            onValueChange = { newValue ->
                if (firstTimeFocusGained.not()) {
                    number = number.copy(selection = newValue.selection)
                } else {
                    firstTimeFocusGained = false
                }

                if (newValue.text == number.text) return@HeroStatNumberField

                if (newValue.text.isEmpty()) {
                    number = newValue

                    return@HeroStatNumberField
                }

                val newNumber = newValue.text.toIntOrNull() ?: run {
                    number = number.copy(
                        text = "",
                        selection = newValue.selection,
                    )

                    return@HeroStatNumberField
                }

                number = newValue.copy(text = min(
                    newNumber,
                    totalPages,
                ).toString(),)
            },
            onFocusReset = {
                firstTimeFocusGained = true

                number = number.copy(selection = TextRange.Zero)

                number.text.toIntOrNull()?.takeIf { it != initialPage }?.let(onCommit)
            },
            onFocusGained = {
                number = number.copy(selection = TextRange(
                    start = 0,
                    end = number.text.length,
                ),)
            },
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    EditorialSuffix(text = "of $totalPages pages")

    Spacer(modifier = Modifier.height(20.dp))

    FocusProgressBar(fraction = (parsed.toFloat() / totalPages).coerceIn(
        0f,
        1f,
    ),)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FocusProgressBar(fraction: Float) {
    LinearWavyProgressIndicator(
        progress = { fraction.coerceIn(
            0f,
            1f,
        ) },
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp),
    )
}
