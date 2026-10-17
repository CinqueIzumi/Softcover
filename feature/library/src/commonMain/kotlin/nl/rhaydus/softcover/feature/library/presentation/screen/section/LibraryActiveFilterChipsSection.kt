package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnActiveFilterChipClickedAction
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryFilterChipRow
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryActiveFilterChips

@Composable
internal fun LibraryActiveFilterChipsSection(
    tabId: String?,
    chips: LibraryActiveFilterChips?,
    runAction: (LibraryAction) -> Unit,
) {
    val shownChips = tabId?.let { id ->
        chips
            ?.takeIf { it.chips.isEmpty.not() }
            ?.let { id to it }
    }

    AnimatedContent(
        targetState = shownChips,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { it != null },
        label = "LibraryActiveFilterChips",
    ) { target ->
        if (target != null) {
            val (targetTabId, targetChips) = target

            Column {
                Spacer(modifier = Modifier.height(Spacing.xs))

                LibraryFilterChipRow(
                    activeFilters = targetChips,
                    onChipEvent = { event ->
                        when (event) {
                            is ChipEvent.Clicked ->
                                runAction(
                                    OnActiveFilterChipClickedAction(
                                        tabId = targetTabId,
                                        key = event.key,
                                    ),
                                )

                            is ChipEvent.Dismissed -> Unit
                            is ChipEvent.SpoilerToggled -> Unit
                        }
                    },
                )
            }
        }
    }
}
