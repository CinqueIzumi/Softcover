package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.explore.presentation.model.FeaturedCardEvent
import nl.rhaydus.softcover.feature.explore.presentation.model.FeaturedCardUiModel

@Composable
internal fun FeaturedSection(
    card: FeaturedCardUiModel?,
    onOpen: () -> Unit,
    onAddToLibrary: () -> Unit,
    onRemoveFromLibrary: () -> Unit,
) {
    if (card == null) return

    FeaturedCard(
        model = card,
        onEvent = { event ->
            when (event) {
                FeaturedCardEvent.Clicked -> onOpen()
                FeaturedCardEvent.WantToReadClicked -> onAddToLibrary()
                FeaturedCardEvent.RemoveFromLibraryClicked -> onRemoveFromLibrary()
            }
        },
        modifier = Modifier.padding(horizontal = Spacing.gutter),
    )
}
