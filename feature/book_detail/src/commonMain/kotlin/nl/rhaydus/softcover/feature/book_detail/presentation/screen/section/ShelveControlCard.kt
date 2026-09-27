package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.secondsToHm
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.icon.RhaydusIconResource
import nl.rhaydus.designsystem.modifier.shakeOnError
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurst
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsReadClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsReadingClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsWantToReadClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import kotlin.math.roundToInt

/**
 * The "Shelve this book" control (design-system.md §5 shelve-rows). A `surfaceContainerLow` card of
 * three VERTICAL rows — Want to read / Reading / Read — replacing the earlier horizontal chip strip.
 * The active row fills `primary`/`onPrimary` and shows a live trailing status; the section opener is
 * the inline 20×1 bar + `eyebrowSmall` contract (never the full section bar). All prior behavior is
 * preserved: optimistic writes, [Modifier.shakeOnError] + "Couldn't save — tap to retry", and the
 * [MarkAsReadBurst] + commit haptic celebration on Read. Every row — Reading included — is enabled
 * even for a not-yet-shelved book: tapping Reading creates the user book directly on Currently
 * Reading in one step (no add-to-Want-to-Read-first).
 */
@Composable
internal fun ShelveControlCard(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    dateStyle: DateStyle,
    celebrationKey: Int,
) {
    SkeletonCrossfade(
        isLoading = state.loadingBookDetails && state.book == null,
        label = "ShelveControlCard",
    ) { loading ->
        if (loading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                InlineAccentLabel(text = "Shelve this book")

                Spacer(modifier = Modifier.height(10.dp))

                // Mirrors the loaded card's own anatomy — the same outer Surface plus three
                // 42dp row-shaped bars at the same 5dp padding / 2dp gap / 11dp corner radius —
                // rather than one oversized blob, so the shimmer resolves into the real rows at
                // an identical height instead of visibly shrinking.
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(15.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(5.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .shimmer(isLoading = true),
                            )
                        }
                    }
                }
            }
        } else {
            val book = state.book

            if (book != null) {
                val status = book.status

                val mutationFailed = book.id in state.failedMutationBookIds

                val haptics = rememberHaptics()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (mutationFailed) {
                            InlineAccentLabel(
                                text = "Couldn't save — tap to retry",
                                color = MaterialTheme.colorScheme.error,
                            )
                        } else {
                            InlineAccentLabel(text = "Shelve this book")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(15.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(5.dp)
                                    .shakeOnError(
                                        trigger = mutationFailed,
                                        onShakeEnd = {
                                            runAction(OnClearMutationFailureAction(bookId = book.id))
                                        },
                                    ),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                ShelveRow(
                                    label = "Want to read",
                                    iconRes = drawableIconResource(
                                        icon = SoftcoverIcon.BookmarkAdd,
                                        contentDescription = "",
                                    ),
                                    selected = status == BookStatus.WantToRead,
                                    onClick = {
                                        haptics.select()
                                        runAction(OnMarkBookAsWantToReadClickAction(book = book))
                                    },
                                )

                                ShelveRow(
                                    label = "Reading",
                                    iconRes = drawableIconResource(
                                        icon = SoftcoverIcon.Reading,
                                        contentDescription = "",
                                    ),
                                    selected = status == BookStatus.Reading,
                                    onClick = {
                                        haptics.select()
                                        runAction(OnMarkBookAsReadingClickAction(book = book))
                                    },
                                    trailingStatus = readingTrailingStatus(book = book),
                                )

                                ShelveRow(
                                    label = "Read",
                                    iconRes = drawableIconResource(
                                        icon = SoftcoverIcon.BookmarkCheck,
                                        contentDescription = "",
                                    ),
                                    selected = status == BookStatus.Read,
                                    onClick = { runAction(OnMarkBookAsReadClickAction(book = book)) },
                                    celebrationKey = celebrationKey,
                                    trailingStatus = readStatusDateTrailing(
                                        book = book,
                                        dateStyle = dateStyle,
                                    ),
                                )
                            }
                        }
                    }

                    MarkAsReadBurst(
                        model = remember(celebrationKey) { MarkAsReadBurstUiModel(triggerKey = celebrationKey) },
                        modifier = Modifier.matchParentSize(),
                    )
                }
            }
        }
    }
}

/** Live "Reading" trailing status: `{pct}% · p. {page}`, time-based for audiobooks. */
private fun readingTrailingStatus(book: Book): String? {
    val read = book.userBookRead ?: return null
    val edition = book.currentEdition
    val pct = read.progress.roundToInt()

    return if (edition?.isAudiobook == true) {
        "$pct% · ${secondsToHm(seconds = read.currentSeconds ?: 0)}"
    } else {
        "$pct% · p. ${read.currentPage ?: 0}"
    }
}

/** Live "Read" trailing status: the read date, when known. */
private fun readStatusDateTrailing(
    book: Book,
    dateStyle: DateStyle,
): String? {
    val userBook = book.userBook ?: return null

    return userBook.getReadDateString(
        style = dateStyle,
        finishedAt = book.userBookRead?.finishedAt,
    )
}

@Composable
private fun ShelveRow(
    label: String,
    iconRes: RhaydusIconResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingStatus: String? = null,
    celebrationKey: Int = 0,
    enabled: Boolean = true,
) {
    val selectedContainer = MaterialTheme.colorScheme.primary
    val unselectedContainer = Color.Transparent
    val selectedContent = MaterialTheme.colorScheme.onPrimary
    val unselectedContent = MaterialTheme.colorScheme.onSurfaceVariant

    val playMotion = playDecorativeMotion()

    var settledSelected by remember { mutableStateOf(selected) }
    val wipe = remember { Animatable(initialValue = 1f) }

    LaunchedEffect(selected) {
        if (selected == settledSelected) return@LaunchedEffect

        if (playMotion.not()) {
            wipe.snapTo(targetValue = 1f)
            settledSelected = selected
            return@LaunchedEffect
        }

        wipe.snapTo(targetValue = 0f)
        wipe.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 180),
        )
        settledSelected = selected
    }

    val fromContainer = if (settledSelected) selectedContainer else unselectedContainer
    val toContainer = if (selected) selectedContainer else unselectedContainer

    val contentColor by animateColorAsState(
        targetValue = if (selected) selectedContent else unselectedContent,
        animationSpec = tween(durationMillis = 180),
        label = "ShelfChipContent",
    )

    val wipeProgress = wipe.value

    val celebration = remember { Animatable(initialValue = 1f) }

    LaunchedEffect(celebrationKey) {
        if (celebrationKey == 0 || playMotion.not()) return@LaunchedEffect

        celebration.snapTo(targetValue = 0f)
        celebration.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400),
        )
    }

    val progress = celebration.value

    val iconScale = if (progress < 0.5f) {
        1f + progress * 2f * 0.25f
    } else {
        1.25f - (progress - 0.5f) * 2f * 0.25f
    }

    val iconReveal = (progress / 0.6f).coerceIn(
        minimumValue = 0f,
        maximumValue = 1f,
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(11.dp))
            .drawBehind {
                drawRect(color = fromContainer)
                clipRect(right = size.width * wipeProgress) {
                    drawRect(color = toContainer)
                }
            },
        color = Color.Transparent,
        contentColor = contentColor,
        shape = RoundedCornerShape(11.dp),
        onClick = onClick,
        enabled = enabled && selected.not(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = iconRes.getIconPainter(),
                contentDescription = iconRes.contentDescription,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
                    .drawWithContent {
                        clipRect(right = size.width * iconReveal) {
                            this@drawWithContent.drawContent()
                        }
                    },
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                ),
            )

            if (selected && trailingStatus != null) {
                Text(
                    text = trailingStatus,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFeatureSettings = "tnum",
                    ),
                    color = contentColor.copy(alpha = 0.9f),
                )
            }
        }
    }
}

/**
 * Small in-flow section label with the 20×1 inline hairline bar + `eyebrowSmall` (design-system.md
 * §2.3 "inline bar" contract). Used for compact labels living inside a card or hero region — here,
 * the "Shelve this book" opener — never mixed with the full [SectionLabel] bar.
 */
@Composable
private fun InlineAccentLabel(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .height(1.dp)
                .width(20.dp)
                .background(color),
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = color,
        )
    }
}
