package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScale
import nl.rhaydus.designsystem.modifier.shakeOnError
import nl.rhaydus.softcover.core.component.badge.CoverOverlay
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryLine
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.prefetchBookDetailOnPress
import nl.rhaydus.softcover.feature.reading.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnShowProgressSheetClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import kotlin.math.roundToInt

/**
 * A secondary "also reading" row (design-system.md §5 "Reading secondary row"): a flat,
 * hairline-topped row (never its own card) holding a small cover, title/author/deadline-readout,
 * a slim wavy progress line, and a trailing compact "set progress" chip.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CompactBookEntry(
    book: Book,
    cover: CoverUiModel?,
    deadlineCoverOverlay: CoverOverlayUiModel?,
    deadlineSummary: DeadlineSummaryUiModel?,
    mutationFailed: Boolean,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progressFraction = (book.userBookRead?.progress ?: 0f) / 100f
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .prefetchBookDetailOnPress(book.id)
                .pointerHandCursor()
                .pressScale(interactionSource)
                .hoverHighlight(interactionSource = interactionSource)
                .shakeOnError(
                    trigger = mutationFailed,
                    onShakeEnd = {
                        runAction(OnClearMutationFailureAction(bookId = book.id))
                    },
                ),
            color = Color.Transparent,
            shape = RectangleShape,
            onClick = { onBookClick(book) },
            interactionSource = interactionSource,
        ) {
            // The deadline readout is a sibling of the cover/text/chip row rather than a line inside
            // the weighted text column: its width is unpredictable (the reader's own date format plus
            // an unbounded pace — a short deadline on a long book reads "1149 pages/day") and grows
            // with the system font scale, so in that column it wrapped on narrower devices. Given the
            // row's full width it stays one line without anything having to measure or guess.
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CoverOverlay(model = deadlineCoverOverlay) {
                        Cover(
                            model = cover,
                            modifier = Modifier.width(54.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(15.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        if (mutationFailed) {
                            Text(
                                text = "Couldn't save — tap to retry".uppercase(),
                                style = MaterialTheme.editorialTypography.eyebrowSmall,
                                color = MaterialTheme.colorScheme.error,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            Spacer(modifier = Modifier.height(2.dp))
                        } else {
                            book.seriesText?.takeIf { it.isNotBlank() }?.let { series ->
                                Text(
                                    text = series.uppercase(),
                                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }

                        Text(
                            text = book.title,
                            style = MaterialTheme.editorialTypography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        book.currentEdition?.authorString?.takeIf { it.isNotBlank() }
                            ?.let { authors ->
                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "By $authors",
                                    style = MaterialTheme.editorialTypography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                        Spacer(modifier = Modifier.height(10.dp))

                        ProgressBlock(
                            progressFraction = progressFraction,
                            percentage = book.userBookRead?.progress,
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    SetProgressChip(
                        onClick = { runAction(OnShowProgressSheetClickAction(book = book)) },
                    )
                }

                if (deadlineSummary != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    DeadlineSummaryLine(model = deadlineSummary)
                }
            }
        }
    }
}

/** Slim wavy progress line + trailing "NN%" — the secondary row's progress indication. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ProgressBlock(
    progressFraction: Float,
    percentage: Float?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        LinearWavyProgressIndicator(
            progress = {
                progressFraction.coerceIn(
                    0f,
                    1f,
                )
            },
            modifier = Modifier
                .weight(1f)
                .height(6.dp),
        )

        Text(
            text = "${(percentage ?: 0f).roundToInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * The compact per-row "open the progress sheet" control (design-system.md §5 "Reading secondary
 * row"): a plain pencil (edit) glyph on a `surfaceContainerHigh` pill. No trailing chevron — that
 * implied a dropdown menu, but the tap always just opens the Update-progress sheet (mark-as-read
 * lives only in the sheet, not a menu on this chip).
 */
@Composable
private fun SetProgressChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DesktopTooltip(text = "Set progress") {
        Surface(
            onClick = onClick,
            modifier = modifier
                .height(38.dp)
                .pointerHandCursor()
                .clearAndSetSemantics {
                    role = Role.Button
                    contentDescription = "Set progress"
                    onClick(label = null) {
                        onClick()
                        true
                    }
                },
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                val editIcon = drawableIconResource(
                    icon = SoftcoverIcon.Edit,
                    contentDescription = "",
                )

                Icon(
                    painter = editIcon.getIconPainter(),
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}
