package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.notification.rememberNotificationPermissionRequester
import nl.rhaydus.softcover.feature.reading.presentation.action.OnStartSessionClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction

@Composable
internal fun rememberSessionStarter(
    books: ImmutableList<Book>,
    runAction: (ReadingAction) -> Unit,
): (Book) -> Unit {
    val currentRunAction by rememberUpdatedState(runAction)
    val currentBooks by rememberUpdatedState(books)

    // An id, not the Book, so the pending start survives the Activity being recreated behind the
    // permission dialog.
    var pendingBookId by rememberSaveable { mutableStateOf<Int?>(null) }

    // The lock-screen surface is a plain notification, so it needs POST_NOTIFICATIONS (Android
    // 13+). Ask at the natural moment — the first session start — then start regardless of the
    // outcome, since the in-app peek bar and Focus Mode work without the permission.
    val requester = rememberNotificationPermissionRequester(
        onResult = {
            currentBooks.firstOrNull { candidate -> candidate.id == pendingBookId }?.let { book ->
                currentRunAction(OnStartSessionClickAction(book = book))
            }

            pendingBookId = null
        },
    )

    return remember(requester) {
        { book: Book ->
            pendingBookId = book.id
            requester.request()
        }
    }
}
