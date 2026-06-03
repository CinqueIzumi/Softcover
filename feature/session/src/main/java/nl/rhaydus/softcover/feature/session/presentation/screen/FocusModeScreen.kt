package nl.rhaydus.softcover.feature.session.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.designsystem.R
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditorialSuffix
import nl.rhaydus.softcover.core.designsystem.presentation.component.HeroStatNumberField
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.designsystem.presentation.session.formatSessionElapsed
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.util.rememberHaptics

/**
 * Distraction-free full-screen reading surface for the active session. An editorial hero on the page
 * surface (§3.3): eyebrow → cover → title → the running timer as a `statHero` hero stat → progress →
 * a quick page-update field → pause/resume + stop. Sized to fit one screen without scrolling; the
 * page field scrolls into view only while the keyboard is open. Reads the shared
 * [ActiveSessionController] directly and pops itself the moment no session is active.
 */
object FocusModeScreen : Screen {
    override val key: String = "focus-mode"

    @Composable
    override fun Content() {
        val controller = koinInject<ActiveSessionController>()
        val navigator = LocalNavigator.currentOrThrow
        val active by controller.activeSession.collectAsStateWithLifecycle()

        val current = active

        if (current == null) {
            navigator.pop()

            return
        }

        FocusModeContent(
            active = current,
            onPauseResume = {
                if (current.session.isPaused) controller.resume() else controller.pause()
            },
            onStop = {
                controller.stop()

                navigator.pop()
            },
            onUpdatePage = { controller.updatePage(newPage = it) },
            onClose = { navigator.pop() },
        )
    }
}

@Composable
private fun FocusModeContent(
    active: ActiveSession,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onUpdatePage: (Int) -> Unit,
    onClose: () -> Unit,
) {
    val haptics = rememberHaptics()
    val isPaused = active.session.isPaused

    var now by remember { mutableStateOf(Clock.System.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = Clock.System.now()

            delay(timeMillis = 1_000)
        }
    }

    val elapsed = active.session.readingDuration(now = now)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize(),
    ) {
        // The activity uses windowSoftInputMode=adjustNothing, so the OS never pans/resizes for the
        // keyboard — Compose's imePadding is the single keyboard-avoidance inset. The Box centres a
        // wrap-height scrolling Column (centred when it fits, scrolls otherwise).
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
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
                        .width(124.dp)
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
                    SoftcoverButton(
                        label = if (isPaused) "Resume" else "Pause",
                        style = ButtonStyle.FILLED,
                        size = ButtonSize.M,
                        icon = SoftcoverIconResource.Drawable(
                            id = if (isPaused) R.drawable.ic_play else R.drawable.ic_pause,
                            contentDescription = "",
                        ),
                        onClick = {
                            haptics.select()

                            onPauseResume()
                        },
                        modifier = Modifier.weight(1f),
                    )

                    SoftcoverButton(
                        label = "Stop",
                        style = ButtonStyle.OUTLINED,
                        size = ButtonSize.M,
                        icon = SoftcoverIconResource.Drawable(
                            id = R.drawable.ic_stop,
                            contentDescription = "",
                        ),
                        onClick = {
                            haptics.threshold()

                            onStop()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Close focus mode",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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

@Composable
private fun FocusProgressBar(fraction: Float) {
    LinearProgressIndicator(
        progress = { fraction.coerceIn(
            0f,
            1f,
        ) },
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp),
        drawStopIndicator = {},
        gapSize = (-2).dp,
    )
}
