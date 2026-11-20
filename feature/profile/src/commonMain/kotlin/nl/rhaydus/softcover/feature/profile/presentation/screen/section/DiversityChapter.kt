package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.chart.ProportionChart
import nl.rhaydus.softcover.core.component.control.ToggleEvent
import nl.rhaydus.softcover.core.component.control.ToggleRow
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.text.FadeThroughText
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.profile.presentation.state.DemographicBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.DiversityChapterUiModel

private const val AUTHOR_STAND_IN = "Yes 24 · No 96 · Unknown 12 · of 132 books"
private const val CHARACTER_STAND_IN = "Yes 24 · Complicated 6 · No 96 · Not answered 12 · of 138 books"

/**
 * The hide switch sits outside the empty-state branch so a reader whose books all lack the detail can
 * always switch it back off.
 */
@Composable
internal fun ColumnScope.DiversityChapter(
    model: DiversityChapterUiModel,
    hideBooksWithoutDetailToggle: ToggleRowUiModel<String>,
    isLoading: Boolean,
    onHideBooksWithoutDetailChange: (Boolean) -> Unit,
) {
    SectionHeader(model = model.header)

    Spacer(modifier = Modifier.height(Spacing.md))

    Column(modifier = Modifier.animateScopeContentSize()) {
        if (model.canHideBooksWithoutDetail && isLoading.not()) {
            ToggleRow(
                model = hideBooksWithoutDetailToggle,
                onEvent = { event ->
                    when (event) {
                        is ToggleEvent.Toggled -> onHideBooksWithoutDetailChange(event.checked)
                    }
                },
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
        }

        if (model.hasContent.not() && isLoading.not()) {
            FadeThroughText(
                text = model.emptyCaption,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            DiversityBars(
                model = model,
                isLoading = isLoading,
            )
        }
    }
}

@Composable
private fun DiversityBars(
    model: DiversityChapterUiModel,
    isLoading: Boolean,
) {
    BreakdownBlock(
        breakdown = model.genderBreakdown,
        isLoading = isLoading,
        standIn = "Across 120 books with the authors' gender.",
    )

    Spacer(modifier = Modifier.height(Spacing.xxl))

    FadeThroughText(
        text = model.scopeCaption,
        style = MaterialTheme.editorialTypography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        isLoading = isLoading,
        standIn = "Each bar counts only the books with that detail.",
    )

    Spacer(modifier = Modifier.height(Spacing.md))

    BreakdownBlock(
        breakdown = model.authorBipoc,
        isLoading = isLoading,
        standIn = AUTHOR_STAND_IN,
    )

    Spacer(modifier = Modifier.height(Spacing.xl))

    BreakdownBlock(
        breakdown = model.authorLgbtq,
        isLoading = isLoading,
        standIn = AUTHOR_STAND_IN,
    )

    Spacer(modifier = Modifier.height(Spacing.xxl))

    BreakdownBlock(
        breakdown = model.characterBipoc,
        isLoading = isLoading,
        standIn = CHARACTER_STAND_IN,
    )

    Spacer(modifier = Modifier.height(Spacing.xl))

    BreakdownBlock(
        breakdown = model.characterLgbtq,
        isLoading = isLoading,
        standIn = CHARACTER_STAND_IN,
    )

    Spacer(modifier = Modifier.height(Spacing.md))

    FadeThroughText(
        text = model.characterSourceCaption,
        style = MaterialTheme.editorialTypography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        isLoading = isLoading,
        standIn = "From your own How it read answers.",
    )

    Spacer(modifier = Modifier.height(Spacing.xxl))

    PublicationSections(
        model = model,
        isLoading = isLoading,
    )
}

@Composable
private fun BreakdownBlock(
    breakdown: DemographicBreakdownUiModel,
    isLoading: Boolean,
    standIn: String,
) {
    Column {
        SoftcoverText(
            text = breakdown.label.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        if (breakdown.hasData || isLoading) {
            ProportionChart(model = breakdown.chart)

            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        FadeThroughText(
            text = breakdown.caption,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            isLoading = isLoading,
            standIn = standIn,
        )
    }
}
