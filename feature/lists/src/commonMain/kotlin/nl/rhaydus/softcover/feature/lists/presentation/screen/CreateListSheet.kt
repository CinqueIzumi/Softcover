package nl.rhaydus.softcover.feature.lists.presentation.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator

/**
 * The public entry point onto the create-list surface — the only thing this module exposes to the
 * app shell (via [nl.rhaydus.softcover.core.designsystem.presentation.navigation.CreateListPresenter]).
 * A plain composable, not a `Screen`: the shell mounts and unmounts it around its own visibility state,
 * and that mount/unmount is what scopes and disposes [CreateListScreen]'s nested
 * [Navigator] — and with it [nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListScreenModel]
 * — to exactly one open/close cycle of the sheet.
 */
@Composable
fun CreateListSheet(
    onDismissRequest: () -> Unit,
    onListCreated: ((listId: Int, listName: String) -> Unit)?,
) {
    Navigator(
        screen = CreateListScreen(
            onDismissRequest = onDismissRequest,
            onListCreated = onListCreated,
        ),
    )
}
