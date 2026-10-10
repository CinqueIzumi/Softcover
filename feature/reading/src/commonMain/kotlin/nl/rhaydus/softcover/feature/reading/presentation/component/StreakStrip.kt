package nl.rhaydus.softcover.feature.reading.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import nl.rhaydus.common.currentLocalDate
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.feature.reading.presentation.mapper.streakSheetFor
import nl.rhaydus.softcover.feature.reading.presentation.mapper.streakStripFor
import nl.rhaydus.softcover.feature.reading.presentation.state.StreakDayRowUiModel
import nl.rhaydus.softcover.feature.reading.presentation.state.StreakSheetUiModel
import nl.rhaydus.softcover.feature.reading.presentation.state.StreakStripUiModel

private const val PLACEHOLDER_DAY_COUNT = 21

@Composable
internal fun StreakStrip(
    model: StreakStripUiModel,
    onEvent: (StreakStripEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHaptics.current

    val litColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)

    Column(
        modifier = modifier,
    ) {
        // A scrolling row keeps every dot comfortably sized on narrow screens instead of
        // squeezing 21 of them into the viewport width. The tap target lives on this row so
        // the press-scale never fires on a horizontal scroll — the inner scroll wins the drag.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressScaleClickable {
                    haptics.select()

                    onEvent(StreakStripEvent.Clicked)
                }
                .horizontalScroll(rememberScrollState())
                .height(18.dp)
                .skeletonMask(visible = model.isLoading),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (model.isLoading && model.days.isEmpty()) {
                repeat(PLACEHOLDER_DAY_COUNT) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(
                                color = trackColor,
                                shape = CircleShape,
                            ),
                    )
                }
            }

            // Most-recent-first: today sits at the left edge, older days run off to the right.
            model.days.forEach { day ->
                Box(
                    modifier = Modifier
                        .size(if (day.isToday) 12.dp else 9.dp)
                        .background(
                            color = if (day.didRead) litColor else trackColor,
                            shape = CircleShape,
                        )
                        .then(
                            if (day.isToday && day.didRead.not()) {
                                Modifier.border(
                                    width = 1.5.dp,
                                    color = litColor,
                                    shape = CircleShape,
                                )
                            } else {
                                Modifier
                            },
                        ),
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // A static, direction-only caption: it never pins to a specific dot, so it stays
            // accurate however far the user scrolls into the past.
            SoftcoverText(
                text = "Most recent first".uppercase(),
                modifier = Modifier.skeletonMask(visible = model.isLoading),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            model.running?.let { running ->
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
                    StatNumber(model = running)
                }
            }
        }
    }
}

@Composable
internal fun StreakStripSheet(
    model: StreakSheetUiModel,
    onDismiss: () -> Unit,
) {
    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.Dismissed -> onDismiss()

                SheetScaffoldEvent.PrimaryClicked,
                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
    ) {
        model.rows.forEach { row ->
            StreakStripSheetRow(row = row)
        }
    }
}

@Composable
private fun StreakStripSheetRow(row: StreakDayRowUiModel) {
    val litColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(
                    color = if (row.didRead) litColor else trackColor,
                    shape = CircleShape,
                ),
        )

        Spacer(modifier = Modifier.width(Spacing.md))

        SoftcoverText(
            text = row.label,
            style = MaterialTheme.editorialTypography.body,
            color = if (row.didRead) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
        )

        if (row.didRead) {
            SoftcoverText(
                text = "Read".uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun previewActivity(litOffsets: Set<Int>): List<ReadingDayActivity> {
    val today = currentLocalDate()
    val firstDay = today.minus(
        20,
        DateTimeUnit.DAY,
    )

    return (0..20).map { offset ->
        ReadingDayActivity(
            date = firstDay.plus(
                offset,
                DateTimeUnit.DAY,
            ),
            didRead = offset in litOffsets,
        )
    }
}

@StandardPreview
@Composable
private fun StreakStripPreview() {
    SoftcoverTheme {
        Column(modifier = Modifier.padding(Spacing.xl)) {
            StreakStrip(
                model = streakStripFor(
                    activity = previewActivity(
                        litOffsets = setOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 19, 20),
                    ),
                ),
                onEvent = {},
            )
        }
    }
}

@StandardPreview
@Composable
private fun StreakStripSheetPreview() {
    SoftcoverTheme {
        val activity = previewActivity(
            litOffsets = setOf(0, 1, 2, 3, 5, 6, 7, 8, 9, 12, 13, 16, 17, 18, 19, 20),
        )

        StreakStripSheet(
            model = streakSheetFor(activity = activity),
            onDismiss = {},
        )
    }
}
