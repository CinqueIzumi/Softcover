package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import kotlin.math.roundToInt

private val GENDER_STACK_ALPHAS = listOf(1f, 0.82f, 0.64f)

/**
 * "Who you read" — a gender proportion bar + legend (the ranked *stack* shape, which the genre
 * section left behind in 3.1.1 — genders partition their population, so a stack is honest there in a
 * way it no longer was for overlapping genres) over two three-way Yes/No/Unknown proportion bars for
 * [AuthorDemographics.bipocBreakdown] and [AuthorDemographics.lgbtqBreakdown]. By default all three
 * bars are scoped to *every* distinct tagged author (not just the ones with that attribute known), so
 * the — often large — untagged population is always visible rather than hidden behind a
 * share-of-tagged-authors percentage: [AuthorDemographics.genderSlices] carries its own
 * [Gender.Unknown] slice pinned last, rendered by [GenderProportionBar]/[GenderLegend] in the same
 * muted, non-`primary` treatment [DemographicProportionBar]/[DemographicLegend] use for their Unknown
 * segment, so gender reads as visually consistent with the other two bars rather than the odd one out.
 *
 * [hideUntaggedAuthors] is the reader's opt-in to the *other* reading: the untagged bucket drops out
 * of each bar and the remaining percentages are renormalised over the tagged authors alone (per bar —
 * see `AuthorDemographics.excludingUntaggedAuthors`, which is applied upstream in the state). The
 * switch renders whenever there is an untagged bucket to drop at all ([canHideUntaggedAuthors]) and
 * sits *outside* the empty-state branch on purpose, so a reader whose authors are entirely untagged
 * can always switch back rather than being stranded in an empty section.
 *
 * Mirroring [GenreRankingSection]/[RatingsHistogramSection], this always renders the [SectionIntro] and
 * falls back to a quiet placeholder line when there is nothing to show, so the section never collapses
 * to nothing between the surrounding `Spacer`s.
 */
@Composable
internal fun AuthorRepresentationSection(
    authorDemographics: AuthorDemographics,
    hideUntaggedAuthors: Boolean,
    canHideUntaggedAuthors: Boolean,
    isLoading: Boolean,
    onHideUntaggedAuthorsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasGenderData = authorDemographics.genderSlices.isNotEmpty()
    val hasBipocData = authorDemographics.bipocBreakdown.total > 0
    val hasLgbtqData = authorDemographics.lgbtqBreakdown.total > 0
    val hasContent = hasGenderData || hasBipocData || hasLgbtqData

    Column(modifier = modifier) {
        SectionIntro(
            eyebrow = "Who you read",
            headline = "The authors behind your shelf",
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (canHideUntaggedAuthors && isLoading.not()) {
            HideUntaggedAuthorsToggle(
                checked = hideUntaggedAuthors,
                onCheckedChange = onHideUntaggedAuthorsChange,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (hasContent.not() && isLoading.not()) {
            Text(
                text = emptyAuthorDemographicsCaption(hideUntaggedAuthors = hideUntaggedAuthors),
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            if (hasGenderData || isLoading) {
                GenderProportionBar(
                    slices = authorDemographics.genderSlices,
                    isLoading = isLoading,
                )

                Spacer(modifier = Modifier.height(16.dp))

                GenderLegend(
                    slices = authorDemographics.genderSlices,
                    isLoading = isLoading,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = genderCaption(
                        knownGenderCount = authorDemographics.knownGenderCount,
                        unknownGenderCount = authorDemographics.unknownGenderCount,
                        hideUntaggedAuthors = hideUntaggedAuthors,
                    ),
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.shimmer(isLoading = isLoading),
                )
            }

            if (hasBipocData || hasLgbtqData || isLoading) {
                if (hasGenderData || isLoading) {
                    Spacer(modifier = Modifier.height(28.dp))
                }

                Text(
                    text = demographicScopeCaption(hideUntaggedAuthors = hideUntaggedAuthors),
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.shimmer(isLoading = isLoading),
                )

                Spacer(modifier = Modifier.height(16.dp))

                DemographicBreakdownBlock(
                    label = "BIPOC",
                    breakdown = authorDemographics.bipocBreakdown,
                    hideUntaggedAuthors = hideUntaggedAuthors,
                    isLoading = isLoading,
                )

                Spacer(modifier = Modifier.height(24.dp))

                DemographicBreakdownBlock(
                    label = "LGBTQ+",
                    breakdown = authorDemographics.lgbtqBreakdown,
                    hideUntaggedAuthors = hideUntaggedAuthors,
                    isLoading = isLoading,
                )
            }
        }
    }
}

// The Appearance-settings toggle-row anatomy (label over an italic Fraunces gloss, trailing M3 Switch,
// no card and no "On/Off" caption) borrowed onto a stats section — the gloss carries the whole
// explanation, since "untagged" means something different per bar and a bare label couldn't say so.
@Composable
private fun HideUntaggedAuthorsToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hide untagged authors",
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Read each bar over the authors it has data for.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

private fun emptyAuthorDemographicsCaption(hideUntaggedAuthors: Boolean): String = if (hideUntaggedAuthors) {
    "None of the authors you've read carry this data yet — switch off to see the untagged share."
} else {
    "Author demographics will appear here as the authors you read get tagged."
}

// Unlike GenreRankedBar (a ranked top-five whose shares overlap and need not sum to 1), every GenderSlice.fraction
// here is a share of *all* distinct authors — including the Gender.Unknown slice pinned last — so the
// slices already sum to (rounding aside) 1 and the bar reads as a true proportion, not a proportion
// among known slices. genderStackIndices below keeps Unknown out of GENDER_STACK_ALPHAS indexing so
// it renders in the same muted, non-`primary` treatment DemographicProportionBar/DemographicLegend use
// for their own Unknown segment, rather than as a fourth stepped-`primary` data segment.
@Composable
private fun GenderProportionBar(
    slices: List<GenderSlice>,
    isLoading: Boolean,
) {
    val shape = RoundedCornerShape(6.dp)
    val stackIndices = genderStackIndices(slices)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
            .clip(shape)
            .shimmer(shape = shape, isLoading = isLoading),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        slices.forEachIndexed { index, slice ->
            Box(
                modifier = Modifier
                    .weight(slice.fraction.toFloat().coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(genderSliceColor(stackIndices[index])),
            )
        }
    }
}

@Composable
private fun GenderLegend(
    slices: List<GenderSlice>,
    isLoading: Boolean,
) {
    val hairlineColor = MaterialTheme.colorScheme.outlineVariant
    val stackIndices = genderStackIndices(slices)

    Column(modifier = Modifier.shimmer(isLoading = isLoading)) {
        slices.forEachIndexed { index, slice ->
            HorizontalDivider(color = hairlineColor)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(genderSliceColor(stackIndices[index])),
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = slice.gender.toDisplayLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = "${(slice.fraction * PERCENTAGE_MULTIPLIER).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Pairs each [GenderSlice] with its stepped-alpha index among the *known* (non-[Gender.Unknown])
 * slices, or `null` for the [Gender.Unknown] slice. [GENDER_STACK_ALPHAS] only ever covers the three
 * known genders (Women/Men/Other), so Unknown — now a real fourth slice — must never be indexed into
 * it; [genderSliceColor] reads `null` as "render like DemographicProportionBar/DemographicLegend's own
 * Unknown segment" instead.
 */
private fun genderStackIndices(slices: List<GenderSlice>): List<Int?> {
    var knownIndex = 0

    return slices.map { slice ->
        if (slice.gender == Gender.Unknown) null else knownIndex++
    }
}

@Composable
private fun genderSliceColor(stackIndex: Int?): Color = if (stackIndex == null) {
    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DEMOGRAPHIC_UNKNOWN_ALPHA)
} else {
    MaterialTheme.colorScheme.primary.copy(
        alpha = GENDER_STACK_ALPHAS.getOrElse(stackIndex) { GENDER_STACK_ALPHAS.last() },
    )
}

/**
 * Maps the unresolvable `gender_id` bucket onto its display word (architecture.md → "Unresolvable API
 * enums"). `Other` is rendered as the plain word "Other" and never narrowed to a specific identity —
 * the bucket mixes non-binary and trans authors, and a narrower label would misgender real people.
 * `Unknown` now renders as the plain word "Unknown" — it is a real, visible slice of
 * [AuthorDemographics.genderSlices] (pinned last) rather than excluded from it.
 */
private fun Gender.toDisplayLabel(): String = when (this) {
    Gender.Female -> "Women"
    Gender.Male -> "Men"
    Gender.Other -> "Other"
    Gender.Unknown -> "Unknown"
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

// The three-way Yes/No/Unknown breakdown bar — a variant of the proportion-bar shape above, but with
// a fixed three-segment order (never ranked) and a deliberately different colour rule: Yes/No are
// tinted `primary` data segments (Yes the stronger fill, No the lighter one), while Unknown renders in
// a muted, non-`primary` neutral (`onSurfaceVariant` at low alpha) so "we don't know" never reads as a
// positive category alongside the two data segments. `total` is coerced to at least 1 so a still-loading
// (all-zero) [DemographicBreakdown] never divides by zero before its first real value arrives.
private const val DEMOGRAPHIC_YES_ALPHA = 1f
private const val DEMOGRAPHIC_NO_ALPHA = 0.55f
private const val DEMOGRAPHIC_UNKNOWN_ALPHA = 0.22f

@Composable
private fun DemographicBreakdownBlock(
    label: String,
    breakdown: DemographicBreakdown,
    hideUntaggedAuthors: Boolean,
    isLoading: Boolean,
) {
    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // An empty breakdown keeps its eyebrow and drops to the caption alone: with nobody tagged there
        // is no proportion to draw, and a bar of three zero-width segments would read as data.
        if (breakdown.total > 0 || isLoading) {
            DemographicProportionBar(
                breakdown = breakdown,
                isLoading = isLoading,
            )

            Spacer(modifier = Modifier.height(16.dp))

            DemographicLegend(
                breakdown = breakdown,
                isLoading = isLoading,
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        Text(
            text = demographicCaption(
                breakdown = breakdown,
                hideUntaggedAuthors = hideUntaggedAuthors,
            ),
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.shimmer(isLoading = isLoading),
        )
    }
}

@Composable
private fun DemographicProportionBar(
    breakdown: DemographicBreakdown,
    isLoading: Boolean,
) {
    val shape = RoundedCornerShape(6.dp)
    val total = breakdown.total.coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
            .clip(shape)
            .shimmer(shape = shape, isLoading = isLoading),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        DemographicBarSegment(
            count = breakdown.yesCount,
            total = total,
            color = MaterialTheme.colorScheme.primary.copy(alpha = DEMOGRAPHIC_YES_ALPHA),
        )

        DemographicBarSegment(
            count = breakdown.noCount,
            total = total,
            color = MaterialTheme.colorScheme.primary.copy(alpha = DEMOGRAPHIC_NO_ALPHA),
        )

        DemographicBarSegment(
            count = breakdown.unknownCount,
            total = total,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DEMOGRAPHIC_UNKNOWN_ALPHA),
        )
    }
}

// A zero bucket draws nothing at all: the 0.01 floor exists so a tiny *real* share stays visible, and
// spending it on an empty bucket would draw a segment for authors who aren't there — which is exactly
// what the Unknown bucket becomes once the untagged authors are hidden.
@Composable
private fun RowScope.DemographicBarSegment(
    count: Int,
    total: Int,
    color: Color,
) {
    if (count > 0) {
        Box(
            modifier = Modifier
                .weight((count.toFloat() / total).coerceAtLeast(0.01f))
                .fillMaxHeight()
                .background(color),
        )
    }
}

@Composable
private fun DemographicLegend(
    breakdown: DemographicBreakdown,
    isLoading: Boolean,
) {
    val hairlineColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = Modifier.shimmer(isLoading = isLoading)) {
        DemographicLegendRow(
            label = "Yes",
            count = breakdown.yesCount,
            total = breakdown.total,
            swatchColor = MaterialTheme.colorScheme.primary.copy(alpha = DEMOGRAPHIC_YES_ALPHA),
            hairlineColor = hairlineColor,
        )

        DemographicLegendRow(
            label = "No",
            count = breakdown.noCount,
            total = breakdown.total,
            swatchColor = MaterialTheme.colorScheme.primary.copy(alpha = DEMOGRAPHIC_NO_ALPHA),
            hairlineColor = hairlineColor,
        )

        // Yes/No are the fixed categories and stay even at 0%, but Unknown is only a row when there is
        // an untagged share to report — otherwise "Unknown 0%" claims a bucket that was deliberately
        // excluded (switch on) or simply doesn't exist (every author tagged).
        if (breakdown.unknownCount > 0 || isLoading) {
            DemographicLegendRow(
                label = "Unknown",
                count = breakdown.unknownCount,
                total = breakdown.total,
                swatchColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DEMOGRAPHIC_UNKNOWN_ALPHA),
                hairlineColor = hairlineColor,
            )
        }
    }
}

@Composable
private fun DemographicLegendRow(
    label: String,
    count: Int,
    total: Int,
    swatchColor: Color,
    hairlineColor: Color,
) {
    val percentage = demographicPercentage(
        count = count,
        total = total,
    )

    HorizontalDivider(color = hairlineColor)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(swatchColor),
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun demographicPercentage(
    count: Int,
    total: Int,
): Int {
    if (total == 0) return 0

    return ((count.toDouble() / total) * PERCENTAGE_MULTIPLIER).roundToInt()
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
