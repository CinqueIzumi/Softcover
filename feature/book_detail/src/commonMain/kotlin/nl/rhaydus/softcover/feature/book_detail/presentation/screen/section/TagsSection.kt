package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import nl.rhaydus.designsystem.layout.ExpandableFlowRow
import nl.rhaydus.designsystem.layout.FlowRowExpansion
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRevealTagSpoilerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

/**
 * The "Tags" section (The Book lens): the community tag block (grouped by category,
 * content-warning tags concealed under [nl.rhaydus.softcover.core.component.chip.ChipTone.Spoiler]
 * until [OnRevealTagSpoilerAction] reveals them in place), followed by the edition colophon line
 * (publisher · format, year · ISBN-13) — folded into one section per the spec, rather than two
 * separate strips.
 */
@Composable
internal fun TagsSection(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val groups = state.communityTagGroups

    val edition = state.displayedEdition
    val colophon = editionColophonLine(edition = edition)
    val hasContent = groups.isNotEmpty() || colophon != null

    // Community tags and the edition colophon are both unknown until the book/edition resolves
    // (rarely before, since neither travels on `initialCover`), so this section is entirely absent
    // through the loading phase — a bare content-dependent condition, exactly like the DS's
    // "Actionable inline banner" precedent, so no skeleton is reserved for it. What DOES need
    // fixing is the pop itself: appearing mid-column with no transition shoves Find it / Voices
    // down in a single frame, so the reveal rides the standard vertical-reveal register instead.
    AnimatedVisibility(
        visible = hasContent,
        enter = expandVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
            fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
        exit = ExitTransition.None,
    ) {
        Column(modifier = Modifier.padding(horizontal = Spacing.gutter)) {
            Spacer(modifier = Modifier.height(Spacing.section))

            if (groups.isNotEmpty()) {
                SectionHeader(model = state.tagsLabel)
            }

            groups.forEach { group ->
                key(group.label) {
                    Spacer(modifier = Modifier.height(Spacing.md))

                    SoftcoverText(
                        text = group.label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    ExpandableFlowRow(
                        modifier = Modifier.animateContentSize(
                            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                        ),
                        collapsedLines = 2,
                        expansion = FlowRowExpansion.Progressive(linesPerExpand = 2),
                        collapsible = true,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        showMoreIndicator = { onExpand ->
                            Chip(
                                model = state.communityTagsShowMoreChip,
                                onEvent = { event ->
                                    if (event is ChipEvent.Clicked) {
                                        onExpand()
                                    }
                                },
                            )
                        },
                        showLessIndicator = { onCollapse ->
                            Chip(
                                model = state.communityTagsShowLessChip,
                                onEvent = { event ->
                                    if (event is ChipEvent.Clicked) {
                                        onCollapse()
                                    }
                                },
                            )
                        },
                    ) {
                        group.chips.forEach { chip ->
                            key(chip.key) {
                                Chip(
                                    model = chip,
                                    onEvent = { event ->
                                        if (event is ChipEvent.Clicked) {
                                            runAction(OnRevealTagSpoilerAction(key = event.key))
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            if (colophon != null) {
                if (groups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                }

                SoftcoverText(
                    text = colophon,
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** `{publisher} · {format}, {year} · ISBN-13 {isbn13}`, keeping only the parts that are known. */
private fun editionColophonLine(edition: BookEdition?): String? {
    if (edition == null) return null

    val publisher = edition.publisher?.takeIf { it.isNotBlank() }

    val formatAndYear = buildString {
        val format = edition.format.takeIf { it.isNotBlank() }
        val year = edition.releaseYear.takeIf { it != -1 && it > 0 }

        if (format != null) append(format)

        if (year != null) {
            if (format != null) append(", ")
            append(year)
        }
    }.takeIf { it.isNotBlank() }

    val isbn13 = edition.isbn13?.takeIf { it.isNotBlank() }?.let { "ISBN-13 $it" }

    val parts = listOfNotNull(publisher, formatAndYear, isbn13)

    return parts.takeIf { it.isNotEmpty() }?.joinToString(separator = " · ")
}
