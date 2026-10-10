package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chart.ProportionChart
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel
import nl.rhaydus.softcover.core.component.control.ToggleEvent
import nl.rhaydus.softcover.core.component.control.ToggleRow
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.text.FadeThroughText
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown

/**
 * "Who you read" — a gender chart over two three-way Yes/No/Unknown charts for
 * [AuthorDemographics.bipocBreakdown] and [AuthorDemographics.lgbtqBreakdown], each a
 * [ProportionChart] built upstream ([genderChart], [bipocChart], [lgbtqChart]). By default all three
 * are scoped to *every* distinct tagged author (not just the ones with that attribute known), so the
 * — often large — untagged population is always visible rather than hidden behind a
 * share-of-tagged-authors percentage. [authorDemographics] still feeds the empty-state checks and the
 * captions.
 *
 * [hideUntaggedAuthors] is the reader's opt-in to the *other* reading: the untagged bucket drops out
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
    authorDemographics: AuthorDemographics,
    genderChart: ProportionChartUiModel,
    bipocChart: ProportionChartUiModel,
    lgbtqChart: ProportionChartUiModel,
    hideUntaggedAuthors: Boolean,
    hideUntaggedAuthorsToggle: ToggleRowUiModel<String>,
    canHideUntaggedAuthors: Boolean,
    isLoading: Boolean,
    header: SectionHeaderUiModel,
    onHideUntaggedAuthorsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasGenderData = authorDemographics.genderSlices.isNotEmpty()
    val hasBipocData = authorDemographics.bipocBreakdown.total > 0
    val hasLgbtqData = authorDemographics.lgbtqBreakdown.total > 0
    val hasContent = hasGenderData || hasBipocData || hasLgbtqData

    Column(modifier = modifier) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(Spacing.md))

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
            SoftcoverText(
                text = emptyAuthorDemographicsCaption(hideUntaggedAuthors = hideUntaggedAuthors),
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            if (hasGenderData || isLoading) {
                ProportionChart(model = genderChart)

                Spacer(modifier = Modifier.height(Spacing.sm))

                FadeThroughText(
                    text = genderCaption(
                        knownGenderCount = authorDemographics.knownGenderCount,
                        unknownGenderCount = authorDemographics.unknownGenderCount,
                        hideUntaggedAuthors = hideUntaggedAuthors,
                    ),
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
                    text = demographicScopeCaption(hideUntaggedAuthors = hideUntaggedAuthors),
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    isLoading = isLoading,
                    standIn = "Shares of the authors each one is tagged for, untagged authors left out.",
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                DemographicBreakdownBlock(
                    label = "BIPOC",
                    chart = bipocChart,
                    breakdown = authorDemographics.bipocBreakdown,
                    hideUntaggedAuthors = hideUntaggedAuthors,
                    isLoading = isLoading,
                )

                Spacer(modifier = Modifier.height(Spacing.xl))

                DemographicBreakdownBlock(
                    label = "LGBTQ+",
                    chart = lgbtqChart,
                    breakdown = authorDemographics.lgbtqBreakdown,
                    hideUntaggedAuthors = hideUntaggedAuthors,
                    isLoading = isLoading,
                )
            }
        }
    }
}

private fun emptyAuthorDemographicsCaption(hideUntaggedAuthors: Boolean): String = if (hideUntaggedAuthors) {
    "None of the authors you've read carry this data yet — switch off to see the untagged share."
} else {
    "Author demographics will appear here as the authors you read get tagged."
}

// The bar/legend already render the untagged share directly as their own Unknown slice, so this no
// longer repeats "M unknown" alongside it — it just grounds the section in the reader's total author
// count, mirroring how DemographicBreakdownBlock's caption grounds its Yes/No/Unknown breakdown. While
// the untagged authors are hidden the count is the same arithmetic (unknownGenderCount is 0 by then)
// but a different claim, so the wording says which authors it counted.
private fun genderCaption(
    knownGenderCount: Int,
    unknownGenderCount: Int,
    hideUntaggedAuthors: Boolean,
): String {
    val totalAuthorCount = knownGenderCount + unknownGenderCount

    if (totalAuthorCount == 0) return "Gender breakdown will fill in as your tagged authors are identified."

    if (hideUntaggedAuthors) return "Across $totalAuthorCount authors with a known gender."

    return "Across $totalAuthorCount authors."
}

// Names what the two breakdowns below are a share *of* — the one line that changes meaning wholesale
// with the switch, since each bar's denominator drops to its own tagged subset.
private fun demographicScopeCaption(hideUntaggedAuthors: Boolean): String = if (hideUntaggedAuthors) {
    "Shares of the authors each one is tagged for — untagged authors left out."
} else {
    "Shares of every author you've read, tagged or not."
}

@Composable
private fun DemographicBreakdownBlock(
    label: String,
    chart: ProportionChartUiModel,
    breakdown: DemographicBreakdown,
    hideUntaggedAuthors: Boolean,
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
        if (breakdown.total > 0 || isLoading) {
            ProportionChart(model = chart)

            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        FadeThroughText(
            text = demographicCaption(
                breakdown = breakdown,
                hideUntaggedAuthors = hideUntaggedAuthors,
            ),
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            isLoading = isLoading,
            standIn = "Yes 24 · No 96 · Unknown 12 · of 132 authors",
        )
    }
}

private fun demographicCaption(
    breakdown: DemographicBreakdown,
    hideUntaggedAuthors: Boolean,
): String {
    if (breakdown.total == 0) {
        if (hideUntaggedAuthors) return "None of your authors carry this data yet."

        return "This breakdown will fill in as your tagged authors are identified."
    }

    val counts = listOfNotNull(
        "Yes ${breakdown.yesCount}",
        "No ${breakdown.noCount}",
        "Unknown ${breakdown.unknownCount}".takeIf { breakdown.unknownCount > 0 },
    ).joinToString(separator = " · ")
    val total = if (hideUntaggedAuthors) {
        "of ${breakdown.total} tagged authors"
    } else {
        "of ${breakdown.total} authors"
    }

    return "$counts · $total"
}
