package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.runtime.Composable
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.softcover.core.component.chip.ChipEvent

/** Wraps [onChipEvent] so a tap or a dismiss gives a light selection tick before it is forwarded. */
@Composable
internal fun hapticChipEvents(onChipEvent: (ChipEvent) -> Unit): (ChipEvent) -> Unit {
    val haptics = LocalHaptics.current

    return { event ->
        when (event) {
            is ChipEvent.Clicked,
            is ChipEvent.Dismissed,
                -> haptics.select()

            is ChipEvent.SpoilerToggled -> Unit
        }

        onChipEvent(event)
    }
}
