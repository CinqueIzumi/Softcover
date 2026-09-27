package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.secondsToHm
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.badge.Badge
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShowUpdateProgressSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import kotlin.math.roundToInt

/**
 * The "In progress" section (Yours lens, `status == Reading`). Section-opener bar + eyebrow, a
 * trailing "Update" pill opening the existing Update-progress sheet, the percent as a `statHero`
 * (tabular), the M3 expressive **wavy** progress indicator (never the flat bar — design-system.md's
 * "wavy always" rule), the `p. x of y` / `n pages to go` counts row, the reading-pace forecast line
 * (hidden when unavailable), and the existing [DeadlineRow] when a deadline is set.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun InProgressSection(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val book = state.book ?: return
    val read = book.userBookRead ?: return
    val edition = book.currentEdition ?: return
    val progress = read.progress
    val isAudiobook = edition.isAudiobook

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(text = "In progress")

            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.primary,
                onClick = { runAction(OnShowUpdateProgressSheetClickAction()) },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val updateProgressIcon = drawableIconResource(
                        icon = SoftcoverIcon.Edit,
                        contentDescription = "Update progress",
                    )

                    Icon(
                        painter = updateProgressIcon.getIconPainter(),
                        contentDescription = updateProgressIcon.contentDescription,
                        modifier = Modifier.size(15.dp),
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Update",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${progress.roundToInt()}%",
            style = MaterialTheme.editorialTypography.statHero,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(10.dp))

        LinearWavyProgressIndicator(
            progress = {
                (progress / 100f).coerceIn(
                    0f,
                    1f,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp),
        )

        Spacer(modifier = Modifier.height(10.dp))

        val summaryLeft: String
        val summaryRight: String

        if (isAudiobook) {
            val totalSeconds = edition.audioSeconds ?: 0
            val currentSeconds = read.currentSeconds ?: 0
            val remainingSeconds = (totalSeconds - currentSeconds).coerceAtLeast(0)

            summaryLeft = "${secondsToHm(currentSeconds)} of ${secondsToHm(totalSeconds)}"
            summaryRight = "${secondsToHm(remainingSeconds)} left"
        } else {
            val pageProgress = read.currentPage ?: 0
            val left = edition.pages?.minus(pageProgress) ?: 0

            summaryLeft = "p. $pageProgress of ${edition.pages ?: 0}"
            summaryRight = "$left pages to go"
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = summaryLeft,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = summaryRight,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Both the pace forecast and the deadline row arrive on their own timers well after the
        // progress block above is already settled (the forecast is a separate network round trip;
        // the deadline collector is independent too) — and either may never arrive at all for a
        // given book. Reserving their height permanently would leave a dead gap far more often than
        // it earns its keep, so both reveal with the app's standard vertical-reveal register (§5
        // "Book-detail in-progress stat") instead of popping in and shoving the row(s) beneath them
        // down in a single frame.
        val playMotion = playDecorativeMotion()
        val revealEnter = if (playMotion) expandVertically() + fadeIn() else EnterTransition.None
        val revealExit = if (playMotion) shrinkVertically() + fadeOut() else ExitTransition.None

        // AnimatedVisibility's exit transition animates whatever the content lambda renders during
        // PostExit — if that reads the live nullable directly, `?.let` collapses to nothing the same
        // recomposition `visible` flips false, so shrinkVertically()/fadeOut() have no content left
        // to shrink/fade and the row disappears in one frame. Render off a "sticky" last-known value
        // instead, updated only while the live value is non-null, so the exiting row keeps its
        // content through the whole shrink/fade.
        val paceForecast = state.readingPaceForecast
        var lastPaceForecast by remember { mutableStateOf(paceForecast) }

        if (paceForecast != null) {
            lastPaceForecast = paceForecast
        }

        AnimatedVisibility(
            visible = paceForecast != null,
            enter = revealEnter,
            exit = revealExit,
        ) {
            lastPaceForecast?.let { forecast ->
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Finish in ${forecast.forecastReadingDays} reading days",
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        val deadlineProgress = state.deadlineProgress
        val deadlineBadge = state.deadlineBadge
        val deadlineRow = if (deadlineProgress != null && deadlineBadge != null) {
            DeadlineRowState(
                progress = deadlineProgress,
                badge = deadlineBadge,
            )
        } else {
            null
        }
        var lastDeadlineRow by remember { mutableStateOf(deadlineRow) }

        if (deadlineRow != null) {
            lastDeadlineRow = deadlineRow
        }

        AnimatedVisibility(
            visible = deadlineProgress != null,
            enter = revealEnter,
            exit = revealExit,
        ) {
            lastDeadlineRow?.let { row ->
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    DeadlineRow(
                        progress = row.progress,
                        badge = row.badge,
                        dateStyle = state.dateStyle,
                    )
                }
            }
        }
    }
}

/**
 * [DeadlineRow]'s progress and badge, remembered as one value so an `AnimatedVisibility` exit
 * cannot animate one half a frame out of sync with the other.
 */
private data class DeadlineRowState(
    val progress: DeadlineProgress,
    val badge: BadgeUiModel,
)

@Composable
private fun DeadlineRow(
    progress: DeadlineProgress,
    badge: BadgeUiModel,
    dateStyle: DateStyle,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 12.dp,
            ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val deadlineIcon = drawableIconResource(
                    icon = SoftcoverIcon.DateRange,
                    contentDescription = "Deadline icon",
                )

                Icon(
                    painter = deadlineIcon.getIconPainter(),
                    contentDescription = deadlineIcon.contentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Finish by ${dateStyle.formatter.format(progress.deadline)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Badge(model = badge)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = buildGoalText(progress = progress),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
private fun buildGoalText(progress: DeadlineProgress): String {
    if (progress.isExpired) {
        val daysPast = -progress.daysRemaining
        val dayLabel = if (daysPast == 1L) "day" else "days"

        return "Deadline passed $daysPast $dayLabel ago."
    }

    val required = ceilToInt(progress.requiredPerDay)
    val dayLabel = if (progress.daysRemaining == 1L) "day" else "days"

    val isSeconds = progress.unit == DeadlineUnit.SECONDS

    val verb = if (isSeconds) "Listen to" else "Read"
    val perDayLabel = if (isSeconds) {
        secondsToHm(required)
    } else {
        val pageLabel = if (required == 1) "page" else "pages"

        "$required $pageLabel"
    }

    val base =
        "$verb $perDayLabel/day for the next ${progress.daysRemaining} $dayLabel to finish on time."

    if (progress.isOnTrack || progress.unitsBehindSchedule <= 0) return base

    val behindLabel = if (isSeconds) {
        secondsToHm(progress.unitsBehindSchedule)
    } else {
        val label = if (progress.unitsBehindSchedule == 1) "page" else "pages"

        "${progress.unitsBehindSchedule} $label"
    }

    return "You're $behindLabel behind schedule — $base"
}

private fun ceilToInt(value: Float): Int {
    val rounded = value.toInt()

    return if (value > rounded) rounded + 1 else rounded
}
