package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chart.ProportionChart
import nl.rhaydus.softcover.core.component.control.ToggleEvent
import nl.rhaydus.softcover.core.component.control.ToggleRow
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.text.FadeThroughText
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.profile.presentation.state.DemographicBreakdownUiModel

/**
 * "Who you read" — a gender chart over two three-way Yes/No/Unknown charts for BIPOC and LGBTQ+
 * authors. By default all three are scoped to *every* distinct tagged author (not just the ones with
 * that attribute known), so the — often large — untagged population is always visible rather than
 * hidden behind a share-of-tagged-authors percentage.
 *
 * The hide switch is the reader's opt-in to the *other* reading: the untagged bucket drops out
 * of each bar and the remaining percentages are renormalised over the tagged authors alone (per bar —
 * see `AuthorDemographics.excludingUntaggedAuthors`, which is applied upstream in the state). The
 * switch renders whenever there is an untagged bucket to drop at all ([canHideUntaggedAuthors]) and
 * sits *outside* the empty-state branch on purpose, so a reader whose authors are entirely untagged
 * can always switch back rather than being stranded in an empty section.
 *
 * Mirroring [GenreRankingSection]/[RatingsHistogramSection], this always renders its section header and
 * falls back to a quiet placeholder line when there is nothing to show, so the section never collapses
 * to nothing between the surrounding `Spacer`s.
 */
@Composable
internal fun AuthorRepresentationSection(
    genderBreakdown: DemographicBreakdownUiModel,
    bipocBreakdown: DemographicBreakdownUiModel,
    lgbtqBreakdown: DemographicBreakdownUiModel,
    scopeCaption: String,
    emptyCaption: String,
    hideUntaggedAuthorsToggle: ToggleRowUiModel<String>,
    canHideUntaggedAuthors: Boolean,
    isLoading: Boolean,
    header: SectionHeaderUiModel,
    onHideUntaggedAuthorsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasGenderData = genderBreakdown.hasData
    val hasBipocData = bipocBreakdown.hasData
    val hasLgbtqData = lgbtqBreakdown.hasData
    val hasContent = hasGenderData || hasBipocData || hasLgbtqData

    Column(modifier = modifier) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(Spacing.md))

        Column(modifier = Modifier.animateScopeContentSize()) {
            if (canHideUntaggedAuthors && isLoading.not()) {
                ToggleRow(
                    model = hideUntaggedAuthorsToggle,
                    onEvent = { event ->
                        when (event) {
                            is ToggleEvent.Toggled -> onHideUntaggedAuthorsChange(event.checked)
                        }
                    },
                )

                Spacer(modifier = Modifier.height(Spacing.xl))
            }

            if (hasContent.not() && isLoading.not()) {
                FadeThroughText(
                    text = emptyCaption,
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                if (hasGenderData || isLoading) {
                    ProportionChart(model = genderBreakdown.chart)

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    FadeThroughText(
                        text = genderBreakdown.caption,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        isLoading = isLoading,
                        standIn = "Across 120 authors with a known gender.",
                    )
                }

                if (hasBipocData || hasLgbtqData || isLoading) {
                    if (hasGenderData || isLoading) {
                        Spacer(modifier = Modifier.height(Spacing.xxl))
                    }

                    FadeThroughText(
                        text = scopeCaption,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        isLoading = isLoading,
                        standIn = "Each bar counts only the authors tagged for it.",
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    DemographicBreakdownBlock(
                        label = "BIPOC",
                        breakdown = bipocBreakdown,
                        isLoading = isLoading,
                    )

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    DemographicBreakdownBlock(
                        label = "LGBTQ+",
                        breakdown = lgbtqBreakdown,
                        isLoading = isLoading,
                    )
                }
            }
        }
    }
}

@Composable
private fun DemographicBreakdownBlock(
    label: String,
    breakdown: DemographicBreakdownUiModel,
    isLoading: Boolean,
) {
    Column {
        SoftcoverText(
            text = label.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        // An empty breakdown keeps its eyebrow and drops to the caption alone: with nobody tagged there
        // is no proportion to draw, and a bar of three zero-width segments would read as data.
        if (breakdown.hasData || isLoading) {
            ProportionChart(model = breakdown.chart)

            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        FadeThroughText(
            text = breakdown.caption,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            isLoading = isLoading,
            standIn = "Yes 24 · No 96 · Unknown 12 · of 132 authors",
        )
    }
}
