package nl.rhaydus.softcover.feature.settings.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowVariant
import nl.rhaydus.softcover.feature.settings.presentation.model.DisplayToggle

/**
 * The Display section's toggle rows, off the composition (`component-contract.md` R9):
 * [showFloatingBarToggle] and [showShelfSwipeToggle] drop the mobile-only rows on desktop; reading
 * streak always shows.
 */
internal fun displayToggleRowsFor(
    showFloatingBarToggle: Boolean,
    showShelfSwipeToggle: Boolean,
    useFloatingBarChecked: Boolean,
    shelfSwipeEnabledChecked: Boolean,
    readingStreakEnabledChecked: Boolean,
): ImmutableList<ToggleRowUiModel<DisplayToggle>> = buildList {
    if (showFloatingBarToggle) {
        add(
            ToggleRowUiModel(
                key = DisplayToggle.FLOATING_BOTTOM_BAR,
                title = "Floating bottom bar",
                gloss = "Float the navigation bar above the screen edge, with rounded corners.",
                checked = useFloatingBarChecked,
                variant = ToggleRowVariant.Inset,
            ),
        )
    }

    if (showShelfSwipeToggle) {
        add(
            ToggleRowUiModel(
                key = DisplayToggle.SHELF_SWIPE,
                title = "Swipe between shelves",
                gloss = "Flick left or right in your Library to move to the next shelf.",
                checked = shelfSwipeEnabledChecked,
                variant = ToggleRowVariant.Inset,
            ),
        )
    }

    add(
        ToggleRowUiModel(
            key = DisplayToggle.READING_STREAK,
            title = "Reading streak",
            gloss = "Show the days you read on the Reading screen.",
            checked = readingStreakEnabledChecked,
            variant = ToggleRowVariant.Inset,
        ),
    )
}.toImmutableList()
