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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import nl.rhaydus.common.currentLocalDate
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.feature.reading.presentation.state.streakSheetScaffoldFor

private val sheetDateFormatter = LocalDate.Format {
    dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
    char(' ')
    day(Padding.NONE)
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
}

@Composable
internal fun StreakStrip(
    activity: List<ReadingDayActivity>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberHaptics()

    val litColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
    val todayDate = activity.lastOrNull()?.date

    // Most-recent-first: today sits at the left edge, older days run off to the right.
    val displayDays = activity.asReversed()

    Column(modifier = modifier.fillMaxWidth()) {
        // A scrolling row keeps every dot comfortably sized on narrow screens instead of
        // squeezing 21 of them into the viewport width. The tap target lives on this row so
        // the press-scale never fires on a horizontal scroll — the inner scroll wins the drag.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressScaleClickable {
                    haptics.select()

                    onClick()
                }
                .horizontalScroll(rememberScrollState())
                .height(18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            displayDays.forEach { day ->
                val isToday = day.date == todayDate

                Box(
                    modifier = Modifier
                        .size(if (isToday) 12.dp else 9.dp)
                        .background(
                            color = if (day.didRead) litColor else trackColor,
                            shape = CircleShape,
                        )
                        .then(
                            if (isToday && day.didRead.not()) {
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

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // A static, direction-only caption: it never pins to a specific dot, so it stays
            // accurate however far the user scrolls into the past.
            Text(
                text = "Most recent first".uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val streakLength = remember(activity) { currentStreakLength(activity) }

            if (streakLength > 0) {
                Text(
                    text = "$streakLength ${if (streakLength == 1) "day" else "days"} running",
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * The number of consecutive read days running backward from today (the last, most-recent entry in
 * [activity]) - a pure UI-layer computation from the same activity list the dots already render, no
 * new domain concept. Stops at the first missed day; `0` when today itself was missed or the list is
 * empty.
 */
internal fun currentStreakLength(activity: List<ReadingDayActivity>): Int {
    var streak = 0

    for (day in activity.asReversed()) {
        if (day.didRead.not()) break

        streak++
    }

    return streak
}

@Composable
internal fun StreakStripSheet(
    activity: List<ReadingDayActivity>,
    scaffold: SheetScaffoldUiModel,
    onDismiss: () -> Unit,
) {
    val today = activity.lastOrNull()?.date

    SheetScaffold(
        model = scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.Dismissed -> onDismiss()

                SheetScaffoldEvent.PrimaryClicked,
                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
    ) {
        activity.asReversed().forEach { day ->
            StreakStripSheetRow(
                day = day,
                today = today,
            )
        }
    }
}

@Composable
private fun StreakStripSheetRow(
    day: ReadingDayActivity,
    today: LocalDate?,
) {
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
                    color = if (day.didRead) litColor else trackColor,
                    shape = CircleShape,
                ),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = sheetDayLabel(
                date = day.date,
                today = today,
            ),
            style = MaterialTheme.editorialTypography.body,
            color = if (day.didRead) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
        )

        if (day.didRead) {
            Text(
                text = "Read".uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun sheetDayLabel(
    date: LocalDate,
    today: LocalDate?,
): String = when (date) {
    today -> "Today"
    today?.minus(
        1,
        DateTimeUnit.DAY,
    ) -> "Yesterday"
    else -> sheetDateFormatter.format(date)
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
        Column(modifier = Modifier.padding(24.dp)) {
            StreakStrip(
                activity = previewActivity(
                    litOffsets = setOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 19, 20),
                ),
                onClick = {},
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
            activity = activity,
            scaffold = streakSheetScaffoldFor(activity = activity),
            onDismiss = {},
        )
    }
}
