package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.component.StaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry

/** Staggers a real card in with [coordinator]; a loading stand-in card renders without the entry animation. */
@Composable
fun Modifier.bookCardStaggeredEntry(
    card: BookCardUiModel,
    coordinator: StaggeredEntryCoordinator,
    index: Int,
): Modifier = if (card.isLoading) {
    this
} else {
    staggeredEntry(
        coordinator = coordinator,
        index = index,
    )
}
