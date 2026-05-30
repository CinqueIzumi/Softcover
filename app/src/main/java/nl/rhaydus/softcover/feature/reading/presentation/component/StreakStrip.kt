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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.presentation.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.presentation.util.rememberHaptics
import nl.rhaydus.softcover.feature.profile.domain.model.ReadingDayActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DOT_ROW_HEIGHT = 18.dp
private val DOT = 9.dp
private val TODAY_DOT = 12.dp
private val DOT_GAP = 10.dp
private val sheetDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM")

@Composable
fun StreakStrip(
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
                .height(DOT_ROW_HEIGHT),
            horizontalArrangement = Arrangement.spacedBy(DOT_GAP),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            displayDays.forEach { day ->
                val isToday = day.date == todayDate

                Box(
                    modifier = Modifier
                        .size(if (isToday) TODAY_DOT else DOT)
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

        // A static, direction-only caption: it never pins to a specific dot, so it stays
        // accurate however far the user scrolls into the past.
        Text(
            text = "Most recent first".uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakStripSheet(
    activity: List<ReadingDayActivity>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        StreakStripSheetContent(activity = activity)
    }
}

@Composable
private fun StreakStripSheetContent(activity: List<ReadingDayActivity>) {
    val readDays = activity.count { it.didRead }
    val today = activity.lastOrNull()?.date

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 24.dp)
            .navigationBarsPadding(),
    ) {
        EditorialSectionHeader(
            eyebrow = "Last ${activity.size} days",
            headline = readDaysSummary(readDays = readDays, totalDays = activity.size),
        )

        Spacer(modifier = Modifier.height(24.dp))

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
                .size(DOT)
                .background(
                    color = if (day.didRead) litColor else trackColor,
                    shape = CircleShape,
                ),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = sheetDayLabel(date = day.date, today = today),
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

private fun readDaysSummary(
    readDays: Int,
    totalDays: Int,
): String = when (readDays) {
    0 -> "No reading logged yet"
    1 -> "You read on 1 of the last $totalDays days"
    else -> "You read on $readDays of the last $totalDays days"
}

private fun sheetDayLabel(
    date: LocalDate,
    today: LocalDate?,
): String = when (date) {
    today -> "Today"
    today?.minusDays(1) -> "Yesterday"
    else -> date.format(sheetDateFormatter)
}

private fun previewActivity(litOffsets: Set<Int>): List<ReadingDayActivity> {
    val today = LocalDate.now()
    val firstDay = today.minusDays(20)

    return (0..20).map { offset ->
        ReadingDayActivity(
            date = firstDay.plusDays(offset.toLong()),
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
        StreakStripSheetContent(
            activity = previewActivity(
                litOffsets = setOf(0, 1, 2, 3, 5, 6, 7, 8, 9, 12, 13, 16, 17, 18, 19, 20),
            ),
        )
    }
}
