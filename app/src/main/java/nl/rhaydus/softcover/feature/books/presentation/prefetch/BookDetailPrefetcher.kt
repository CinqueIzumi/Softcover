package nl.rhaydus.softcover.feature.books.presentation.prefetch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import java.util.Collections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.feature.books.domain.usecase.FetchBookByIdUseCase
import org.koin.compose.koinInject
import timber.log.Timber

private const val DefaultPrefetchDebounceMillis = 300L

class BookDetailPrefetcher(
    private val fetchBookByIdUseCase: FetchBookByIdUseCase,
    private val scope: CoroutineScope,
) {
    // Dedupes prefetches across cards within a screen session. Apollo's CacheFirst
    // would no-op the network call anyway, but skipping the coroutine and the
    // parallel editions query saves real work on re-entry into a populated list.
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
    val scope = rememberCoroutineScope()

    return remember(useCase, scope) {
        BookDetailPrefetcher(
            fetchBookByIdUseCase = useCase,
            scope = scope,
        )
    }
}

@Composable
fun PrefetchBookDetailOnVisible(
    bookId: Int,
    debounceMillis: Long = DefaultPrefetchDebounceMillis,
) {
    val prefetcher = LocalBookDetailPrefetcher.current ?: return

    LaunchedEffect(
        bookId,
        prefetcher,
    ) {
        delay(debounceMillis)
        prefetcher.prefetch(bookId)
    }
}
