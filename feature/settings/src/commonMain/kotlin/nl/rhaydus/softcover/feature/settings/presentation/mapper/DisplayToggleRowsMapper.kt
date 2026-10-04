package nl.rhaydus.softcover.feature.settings.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.feature.settings.presentation.model.DisplayToggle
import nl.rhaydus.softcover.feature.settings.presentation.model.SettingsToggleRowUiModel
import nl.rhaydus.softcover.feature.settings.presentation.model.settingsSwitchToggle

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
): ImmutableList<SettingsToggleRowUiModel<DisplayToggle>> = buildList {
    if (showFloatingBarToggle) {
        add(
            SettingsToggleRowUiModel(
                key = DisplayToggle.FLOATING_BOTTOM_BAR,
                label = "Floating bottom bar",
                gloss = "Lift the nav off the edge, with rounded corners.",
                toggle = settingsSwitchToggle(
                    key = DisplayToggle.FLOATING_BOTTOM_BAR.name,
                    label = "Floating bottom bar",
                    checked = useFloatingBarChecked,
                ),
            ),
        )
    }

    if (showShelfSwipeToggle) {
        add(
            SettingsToggleRowUiModel(
                key = DisplayToggle.SHELF_SWIPE,
                label = "Swipe between shelves",
                gloss = "Flick left or right in your Library to move to the next shelf.",
                toggle = settingsSwitchToggle(
                    key = DisplayToggle.SHELF_SWIPE.name,
                    label = "Swipe between shelves",
                    checked = shelfSwipeEnabledChecked,
                ),
            ),
        )
    }

    add(
        SettingsToggleRowUiModel(
            key = DisplayToggle.READING_STREAK,
            label = "Reading streak",
            gloss = "Count the days you read in a row, shown on your profile.",
            toggle = settingsSwitchToggle(
                key = DisplayToggle.READING_STREAK.name,
                label = "Reading streak",
                checked = readingStreakEnabledChecked,
            ),
        ),
    )
}.toImmutableList()
