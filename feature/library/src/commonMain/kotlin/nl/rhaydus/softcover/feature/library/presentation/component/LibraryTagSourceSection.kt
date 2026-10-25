package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import nl.rhaydus.designsystem.layout.ExpandableFlowRow
import nl.rhaydus.designsystem.layout.FlowRowExpansion
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterSheetEvent
import nl.rhaydus.softcover.feature.library.presentation.state.TagSourceSummary

internal fun LazyListScope.tagSourceItems(
    summary: TagSourceSummary,
    leadingDivider: Boolean,
    onChipEvent: (ChipEvent) -> Unit,
    onEvent: (LibraryFilterSheetEvent) -> Unit,
) {
    val keyPrefix = summary.source.name

    val categoryClick: (String) -> Unit = { key ->
        val picked = LibraryFilterSheetEvent.TagCategoryPicked(
            source = summary.source,
            key = key,
        )

        onEvent(picked)
    }

    item(key = "$keyPrefix:top") {
        TagSourceTop(
            modifier = Modifier,
            summary = summary,
            leadingDivider = leadingDivider,
            onChipEvent = onChipEvent,
            onEvent = onEvent,
        )
    }

    item(key = "$keyPrefix:body") {
        TagSourceBody(
            modifier = Modifier,
            summary = summary,
            onChipEvent = onChipEvent,
            onCategoryClick = categoryClick,
        )
    }

    item(key = "$keyPrefix:end") {
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}

@Composable
private fun TagSourceTop(
    modifier: Modifier,
    summary: TagSourceSummary,
    leadingDivider: Boolean,
    onChipEvent: (ChipEvent) -> Unit,
    onEvent: (LibraryFilterSheetEvent) -> Unit,
) {
    val hapticChipEvent = hapticChipEvents(onChipEvent)
    val sizeSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()

    Column(modifier = modifier.animateContentSize(animationSpec = sizeSpec)) {
        if (leadingDivider) {
            Divider(modifier = Modifier.padding(bottom = Spacing.xl))
        }

        SectionHeader(
            model = summary.header,
            trailing = summary.chosenLabel?.let { label ->
                {
                    SoftcoverText(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )

        summary.gloss?.let { gloss ->
            Spacer(modifier = Modifier.height(Spacing.xxs))

            SoftcoverText(
                text = gloss,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (summary.chosenChips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            ExpandableFlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                summary.chosenChips.forEach { chip ->
                    key(chip.label) {
                        Chip(
                            model = chip,
                            onEvent = hapticChipEvent,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        TextField(
            model = summary.searchField,
            onEvent = { event ->
                when (event) {
                    is TextFieldEvent.ValueChanged -> onEvent(
                        LibraryFilterSheetEvent.TagSearchChanged(
                            source = summary.source,
                            query = event.text,
                        ),
                    )

                    is TextFieldEvent.TrailingClicked -> onEvent(
                        LibraryFilterSheetEvent.TagSearchChanged(
                            source = summary.source,
                            query = "",
                        ),
                    )

                    is TextFieldEvent.Submitted,
                    is TextFieldEvent.ShakeEnded -> Unit
                }
            },
        )
    }
}

@Composable
private fun TagSourceBody(
    modifier: Modifier,
    summary: TagSourceSummary,
    onChipEvent: (ChipEvent) -> Unit,
    onCategoryClick: (String) -> Unit,
) {
    val hapticChipEvent = hapticChipEvents(onChipEvent)
    val sizeSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()

    Column(modifier = modifier.animateContentSize(animationSpec = sizeSpec)) {
        if (summary.categoryChips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing.md))

            FacetChipLine {
                summary.categoryChips.forEach { chip ->
                    Chip(
                        model = chip,
                        onEvent = { event ->
                            when (event) {
                                is ChipEvent.Clicked -> onCategoryClick(event.key)
                                is ChipEvent.Dismissed -> Unit
                                is ChipEvent.SpoilerToggled -> Unit
                            }
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        summary.matchHeader?.let { header ->
            SectionHeader(model = header)

            Spacer(modifier = Modifier.height(Spacing.xs))
        }

        summary.emptyLine?.let { line ->
            SoftcoverText(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (summary.tagChips.isNotEmpty()) {
            ExpandableFlowRow(
                collapsedLines = 2,
                expansion = FlowRowExpansion.Progressive(linesPerExpand = 2),
                collapsible = true,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                showMoreIndicator = { onExpand ->
                    Chip(
                        model = summary.showMoreChip,
                        onEvent = { event ->
                            if (event is ChipEvent.Clicked) {
                                onExpand()
                            }
                        },
                    )
                },
                showLessIndicator = { onCollapse ->
                    Chip(
                        model = summary.showLessChip,
                        onEvent = { event ->
                            if (event is ChipEvent.Clicked) {
                                onCollapse()
                            }
                        },
                    )
                },
            ) {
                summary.tagChips.forEach { chip ->
                    key(chip.key) {
                        Chip(
                            model = chip,
                            onEvent = hapticChipEvent,
                        )
                    }
                }
            }
        }
    }
}
