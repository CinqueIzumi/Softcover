package nl.rhaydus.softcover.feature.lists.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import cafe.adriel.voyager.navigator.Navigator

/**
 * The public entry point onto the create-list surface — the only thing this module exposes to the
 * app shell (via [nl.rhaydus.softcover.core.presentation.navigation.CreateListPresenter]).
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
    val latestOnDismissRequest by rememberUpdatedState(onDismissRequest)
    val latestOnListCreated by rememberUpdatedState(onListCreated)
    val reportsCreation = onListCreated != null
    val callbacks = remember(reportsCreation) {
        CreateListCallbacks(
            onDismissRequest = { latestOnDismissRequest() },
            onListCreated = if (reportsCreation) {
                { listId, listName ->
                    latestOnListCreated?.invoke(
                        listId,
                        listName,
                    )
                }
            } else {
                null
            },
        )
    }

    CompositionLocalProvider(LocalCreateListCallbacks provides callbacks) {
        Navigator(
            screen = CreateListScreen(),
            onBackPressed = null,
        )
    }
}
