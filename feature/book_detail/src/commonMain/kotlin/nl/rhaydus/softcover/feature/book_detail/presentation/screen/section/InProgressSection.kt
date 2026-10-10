package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.deadline.DeadlineRow
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.progress.ProgressBar
import nl.rhaydus.softcover.core.component.state.RevealedWhenPresent
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShowUpdateProgressSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

/**
 * The "In progress" section (Yours lens, `status == Reading`). Section-opener bar + eyebrow, a
 * trailing "Update" pill opening the existing Update-progress sheet, the percent as a `statHero`
 * (tabular), the M3 expressive **wavy** progress indicator (never the flat bar — design-system.md's
 * "wavy always" rule), the `p. x of y` / `n pages to go` counts row, the reading-pace forecast line
 * (hidden when unavailable), and the [DeadlineRow] when a deadline is set.
 */
@Composable
internal fun InProgressSection(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val inProgress = state.inProgress ?: return

    Column(modifier = Modifier.padding(horizontal = Spacing.gutter)) {
        SectionHeader(model = state.inProgressSectionHeader) {
            PillButton(
                model = state.updateProgressPill,
                onEvent = { runAction(OnShowUpdateProgressSheetClickAction()) },
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            StatNumber(model = inProgress.percent)
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        ProgressBar(fraction = inProgress.fraction)

        Spacer(modifier = Modifier.height(Spacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SoftcoverText(
                text = inProgress.summaryStart,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SoftcoverText(
                text = inProgress.summaryEnd,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        RevealedWhenPresent(value = state.readingPaceForecast) { forecast ->
            Column {
                Spacer(modifier = Modifier.height(Spacing.sm))

                SoftcoverText(
                    text = "Finish in ${forecast.forecastReadingDays} reading days",
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        RevealedWhenPresent(value = state.deadlineRow) { row ->
            Column {
                Spacer(modifier = Modifier.height(Spacing.md))

                DeadlineRow(model = row)
            }
        }
    }
}
