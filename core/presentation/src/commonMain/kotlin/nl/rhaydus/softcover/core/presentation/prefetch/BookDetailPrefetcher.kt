package nl.rhaydus.softcover.core.presentation.prefetch

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.compose.koinInject
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.core.domain.model.ApplicationScope

class BookDetailPrefetcher internal constructor(
    private val fetchBookByIdUseCase: FetchBookByIdUseCase,
    private val scope: CoroutineScope,
) {
    // Dedupes prefetches within a screen session so a user repeatedly pressing
    // the same card does not refire the network call. Apollo's CacheFirst would
    // no-op the request, but skipping the coroutine and the parallel editions
    // query saves real work. The set is guarded by [mutex] because the dedup
    // check (gesture thread) and the rollback (scope dispatcher) can race.
    private val prefetched = mutableSetOf<Int>()
    private val mutex = Mutex()

    fun prefetch(bookId: Int) {
        scope.launch {
            val isNew = mutex.withLock { prefetched.add(bookId) }
            if (isNew.not()) return@launch

            fetchBookByIdUseCase(id = bookId).onFailure { error ->
                AppLog.w("Book-detail prefetch failed for $bookId: $error")

                mutex.withLock { prefetched.remove(bookId) }
            }
        }
    }
}

val LocalBookDetailPrefetcher = compositionLocalOf<BookDetailPrefetcher?> { null }

@Composable
fun rememberBookDetailPrefetcher(): BookDetailPrefetcher {
    val useCase = koinInject<FetchBookByIdUseCase>()
    val applicationScope = koinInject<ApplicationScope>()

    return remember(useCase, applicationScope) {
        BookDetailPrefetcher(
            fetchBookByIdUseCase = useCase,
            scope = applicationScope.scope,
        )
    }
}

// Fires the prefetch on finger-down rather than on a successful click so the
// network round-trip overlaps with the user lifting their finger and the
// subsequent navigation transition. `awaitFirstDown(requireUnconsumed = false)`
// observes the press without consuming it, so any sibling `clickable` /
// `pressScaleClickable` modifier still receives the gesture normally.
fun Modifier.prefetchBookDetailOnPress(bookId: Int): Modifier = composed {
    val prefetcher = LocalBookDetailPrefetcher.current ?: return@composed this

    pointerInput(
        bookId,
        prefetcher,
    ) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            prefetcher.prefetch(bookId)
        }
    }
}
