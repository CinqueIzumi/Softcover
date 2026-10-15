package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.icon.RhaydusIconResource
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurst
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.modifier.rememberPulseScaleProvider
import nl.rhaydus.softcover.core.component.modifier.shakeOnError
import nl.rhaydus.softcover.core.component.state.clippedSkeletonMask
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.tabularFigures
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsPausedClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsReadClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsReadingClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsWantToReadClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShelveReadingStatusUiModel

/** Book detail's shelve control, specified in `docs/reference/design-system/patterns/book-detail.md` § Shelve control. */
@Composable
internal fun ShelveControlCard(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val book = state.book
    val isLoading = state.loadingBookDetails && book == null

    if (book == null && isLoading.not()) return

    val status = state.shelfStatus
    val mutationFailed = book != null && book.id in state.failedMutationBookIds

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.gutter),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(model = state.shelveControlHeader)

            Spacer(modifier = Modifier.height(Spacing.md))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = Corners.lg,
            ) {
                Column(
                    modifier = Modifier
                        .padding(Spacing.xxs)
                        .shakeOnError(
                            trigger = mutationFailed,
                            onShakeEnd = {
                                book?.let { runAction(OnClearMutationFailureAction(bookId = it.id)) }
                            },
                        ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
                ) {
                    ShelveRow(
                        label = "Want to read",
                        iconRes = drawableIconResource(
                            icon = SoftcoverIcon.BookmarkAdd,
                            contentDescription = "",
                        ),
                        selected = status == BookStatus.WantToRead,
                        isLoading = isLoading,
                        onClick = {
                            book?.let {
                                runAction(OnMarkBookAsWantToReadClickAction(book = it))
                            }
                        },
                    )

                    ShelveRow(
                        label = "Reading",
                        iconRes = drawableIconResource(
                            icon = SoftcoverIcon.Reading,
                            contentDescription = "",
                        ),
                        selected = status == BookStatus.Reading,
                        isLoading = isLoading,
                        onClick = {
                            book?.let {
                                runAction(OnMarkBookAsReadingClickAction(book = it))
                            }
                        },
                        readingStatus = state.shelveReadingStatus,
                    )

                    ShelveRow(
                        label = "Paused",
                        iconRes = drawableIconResource(
                            icon = SoftcoverIcon.Pause,
                            contentDescription = "",
                        ),
                        selected = status == BookStatus.Paused,
                        isLoading = isLoading,
                        onClick = {
                            book?.let {
                                runAction(OnMarkBookAsPausedClickAction(book = it))
                            }
                        },
                        readingStatus = state.shelveReadingStatus,
                    )

                    ShelveRow(
                        label = "Read",
                        iconRes = drawableIconResource(
                            icon = SoftcoverIcon.BookmarkCheck,
                            contentDescription = "",
                        ),
                        selected = status == BookStatus.Read,
                        isLoading = isLoading,
                        onClick = { book?.let { runAction(OnMarkBookAsReadClickAction(book = it)) } },
                        celebrationKey = state.markAsReadBurst.triggerKey,
                        trailingStatus = state.shelveReadDate,
                    )
                }
            }
        }

        MarkAsReadBurst(
            model = state.markAsReadBurst,
            modifier = Modifier.matchParentSize(),
        )
    }
}

@Composable
private fun ShelveRow(
    label: String,
    iconRes: RhaydusIconResource,
    selected: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingStatus: String? = null,
    readingStatus: ShelveReadingStatusUiModel? = null,
    celebrationKey: Int = 0,
    enabled: Boolean = true,
) {
    val selectedContainer = MaterialTheme.colorScheme.primary
    val unselectedContainer = Color.Transparent
    val selectedContent = MaterialTheme.colorScheme.onPrimary
    val unselectedContent = MaterialTheme.colorScheme.onSurfaceVariant

    val wipeSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val celebrationSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()

    var settledSelected by remember { mutableStateOf(selected) }
    var wasLoading by remember { mutableStateOf(isLoading) }
    val wipe = remember(selected) {
        Animatable(initialValue = if (isLoading || wasLoading || selected == settledSelected) 1f else 0f)
    }

    // Landing from the loading state adopts the shelved row as already selected, so it plays no wipe.
    LaunchedEffect(selected, isLoading) {
        if (isLoading || wasLoading) {
            settledSelected = selected
            wasLoading = isLoading
            wipe.snapTo(targetValue = 1f)

            return@LaunchedEffect
        }

        if (selected == settledSelected) return@LaunchedEffect

        wipe.animateTo(
            targetValue = 1f,
            animationSpec = wipeSpec,
        )
        settledSelected = selected
    }

    val fromContainer = if (settledSelected) selectedContainer else unselectedContainer
    val toContainer = if (selected) selectedContainer else unselectedContainer

    val contentColor by animateColorAsState(
        targetValue = if (selected) selectedContent else unselectedContent,
        animationSpec = if (wasLoading) snap() else MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "ShelfChipContent",
    )

    val celebration = remember { Animatable(initialValue = 1f) }

    val initialCelebrationKey = remember { celebrationKey }

    LaunchedEffect(celebrationKey) {
        if (celebrationKey == initialCelebrationKey) return@LaunchedEffect

        celebration.snapTo(targetValue = 0f)
        celebration.animateTo(
            targetValue = 1f,
            animationSpec = celebrationSpec,
        )
    }

    val iconScale = rememberPulseScaleProvider(
        trigger = celebrationKey,
        peakScale = 1.25f,
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .clippedSkeletonMask(
                visible = isLoading,
                shape = Corners.md,
            )
            .drawBehind {
                val edge = size.width * wipe.value

                clipRect(right = edge) {
                    drawRect(color = toContainer)
                }

                clipRect(left = edge) {
                    drawRect(color = fromContainer)
                }
            },
        color = Color.Transparent,
        contentColor = contentColor,
        shape = Corners.md,
        onClick = onClick,
        enabled = enabled && isLoading.not() && selected.not(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = iconRes.getIconPainter(),
                contentDescription = iconRes.contentDescription,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer {
                        val scale = iconScale()

                        scaleX = scale
                        scaleY = scale
                    }
                    .drawWithContent {
                        val iconReveal = (celebration.value / 0.6f).coerceIn(
                            minimumValue = 0f,
                            maximumValue = 1f,
                        )

                        clipRect(right = size.width * iconReveal) {
                            this@drawWithContent.drawContent()
                        }
                    },
            )

            Spacer(modifier = Modifier.width(Spacing.sm))

            SoftcoverText(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                ),
            )

            if (selected && trailingStatus != null) {
                SoftcoverText(
                    text = trailingStatus,
                    style = MaterialTheme.typography.labelSmall.tabularFigures(),
                    color = contentColor.copy(alpha = 0.9f),
                )
            }

            if (selected && readingStatus != null) {
                CompositionLocalProvider(LocalContentColor provides contentColor.copy(alpha = 0.9f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        StatNumber(model = readingStatus.percent)

                        StatNumber(model = readingStatus.position)
                    }
                }
            }
        }
    }
}
