package nl.rhaydus.softcover.feature.lists.presentation.screen

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The sheet host's callbacks, handed to [CreateListScreen] through [LocalCreateListCallbacks] because
 * lambdas cannot ride in a Screen's saved (Serializable) state and a restored screen would lose them.
 */
@Immutable
internal class CreateListCallbacks(
    val onDismissRequest: () -> Unit,
    val onListCreated: ((listId: Int, listName: String) -> Unit)?,
)

internal val LocalCreateListCallbacks = staticCompositionLocalOf<CreateListCallbacks> {
    error("CreateListScreen must be hosted by CreateListSheet")
}
