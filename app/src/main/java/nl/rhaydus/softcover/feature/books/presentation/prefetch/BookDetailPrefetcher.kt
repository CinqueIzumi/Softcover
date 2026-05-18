package nl.rhaydus.softcover.feature.books.presentation.prefetch

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import java.util.Collections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.feature.books.domain.usecase.FetchBookByIdUseCase
import org.koin.compose.koinInject
import timber.log.Timber

class BookDetailPrefetcher(
    private val fetchBookByIdUseCase: FetchBookByIdUseCase,
    private val scope: CoroutineScope,
) {
    // Dedupes prefetches within a screen session so a user repeatedly pressing
    // the same card does not refire the network call. Apollo's CacheFirst would
    // no-op the request, but skipping the coroutine and the parallel editions
    // query saves real work.
    private val prefetched = Collections.synchronizedSet(mutableSetOf<Int>())

    fun prefetch(bookId: Int) {
        if (prefetched.add(bookId).not()) return

        scope.launch {
            fetchBookByIdUseCase(id = bookId).onFailure { error ->
                Timber.w("-=- Book-detail prefetch failed for $bookId: $error")

                prefetched.remove(bookId)
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
