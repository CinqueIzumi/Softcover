package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.editorial.component.EditorialSearchField
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSearchQueryChangeAction
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryControlLine
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

@Composable
internal fun LibrarySearchAndControls(
    state: LibraryUiState,
    tab: LibraryTab?,
    runAction: (LibraryAction) -> Unit,
) {
    Column {
        Spacer(modifier = Modifier.height(Spacing.xxs))

        EditorialSearchField(
            query = state.searchQuery,
            onQueryChange = { query ->
                runAction(OnSearchQueryChangeAction(query = query))
            },
            onClearClick = {
                runAction(OnSearchQueryChangeAction(query = ""))
            },
            searchIcon = drawableIconResource(
                icon = SoftcoverIcon.Search,
                contentDescription = "Search",
            ),
            clearIcon = drawableIconResource(
                icon = SoftcoverIcon.Close,
                contentDescription = "Clear search",
            ),
            placeholder = "Search your collection",
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        LibraryControlLine(
            state = state,
            tab = tab,
            runAction = runAction,
        )
    }
}
