package nl.rhaydus.softcover.feature.widget.presentation.mapper

import android.net.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withTimeoutOrNull
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickResolution
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSource
import nl.rhaydus.softcover.feature.widget.presentation.model.RandomPickWidgetUiModel
import kotlin.time.Duration.Companion.milliseconds

private class RandomPickWidgetInputs(
    val isSignedIn: Boolean,
    val themeConfiguration: ThemeConfiguration,
    val wantToRead: List<Book>,
    val lists: List<BookList>,
)

/**
 * A picked book emits its text first with the cover pending, then again with its image files. Files that already
 * exist resolve within [IMAGE_HIT_WINDOW] and skip the pending step. A failed resolve is logged and the last
 * model stays.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun randomPickWidgetModels(
    isSignedIn: Flow<Boolean>,
    themeConfiguration: Flow<ThemeConfiguration>,
    wantToRead: Flow<List<Book>>,
    lists: Flow<List<BookList>>,
    changes: Flow<Unit>,
    resolve: suspend (wantToRead: List<Book>, lists: List<BookList>) -> Result<RandomPickResolution>,
    coverUri: suspend (Book) -> Uri?,
    artwork: suspend (Book) -> RandomPickWidgetUiModel.Artwork?,
): Flow<RandomPickWidgetUiModel> = combine(
    isSignedIn,
    themeConfiguration,
    wantToRead,
    lists,
    changes,
) { signedIn, theme, books, userLists, _ ->
    RandomPickWidgetInputs(
        isSignedIn = signedIn,
        themeConfiguration = theme,
        wantToRead = books,
        lists = userLists,
    )
}.transformLatest { inputs ->
    if (inputs.isSignedIn.not()) {
        emit(inputs.model(RandomPickWidgetUiModel.Content.SignedOut))
        return@transformLatest
    }

    val resolution = resolve(
        inputs.wantToRead,
        inputs.lists,
    ).getOrElse { error ->
        AppLog.e(
            error,
            "Resolving the random pick failed",
        )
        return@transformLatest
    }

    when (resolution) {
        RandomPickResolution.SourceUnavailable -> {
            emit(inputs.model(RandomPickWidgetUiModel.Content.SourceUnavailable))
        }

        is RandomPickResolution.Resolved -> emitResolved(
            inputs = inputs,
            resolution = resolution,
            coverUri = coverUri,
            artwork = artwork,
        )
    }
}

private suspend fun FlowCollector<RandomPickWidgetUiModel>.emitResolved(
    inputs: RandomPickWidgetInputs,
    resolution: RandomPickResolution.Resolved,
    coverUri: suspend (Book) -> Uri?,
    artwork: suspend (Book) -> RandomPickWidgetUiModel.Artwork?,
) {
    val eyebrow = eyebrowFor(
        source = resolution.source,
        lists = inputs.lists,
    )

    val book = resolution.book

    if (book == null) {
        emit(inputs.model(RandomPickWidgetUiModel.Content.EmptyPool(eyebrow = eyebrow)))
        return
    }

    coroutineScope {
        val cover = async { coverUri(book) }
        val art = async { artwork(book) }

        val ready = withTimeoutOrNull(IMAGE_HIT_WINDOW) { cover.await() to art.await() }

        if (ready == null) {
            emit(
                inputs.model(
                    picked(
                        eyebrow = eyebrow,
                        book = book,
                        cover = RandomPickWidgetUiModel.Cover.Pending,
                        artwork = null,
                    ),
                ),
            )
        }

        val (uri, resolvedArtwork) = ready ?: (cover.await() to art.await())

        emit(
            inputs.model(
                picked(
                    eyebrow = eyebrow,
                    book = book,
                    cover = uri.toCover(),
                    artwork = resolvedArtwork,
                ),
            ),
        )
    }
}

private fun RandomPickWidgetInputs.model(content: RandomPickWidgetUiModel.Content) = RandomPickWidgetUiModel(
    themeConfiguration = themeConfiguration,
    content = content,
)

private fun picked(
    eyebrow: String,
    book: Book,
    cover: RandomPickWidgetUiModel.Cover,
    artwork: RandomPickWidgetUiModel.Artwork?,
) = RandomPickWidgetUiModel.Content.Picked(
    eyebrow = eyebrow,
    bookId = book.id,
    title = book.title,
    author = book.authorString,
    cover = cover,
    artwork = artwork,
)

private fun Uri?.toCover(): RandomPickWidgetUiModel.Cover = if (this == null) {
    RandomPickWidgetUiModel.Cover.Missing
} else {
    RandomPickWidgetUiModel.Cover.Loaded(uri = this)
}

private val IMAGE_HIT_WINDOW = 150.milliseconds

internal fun eyebrowFor(
    source: RandomPickSource,
    lists: List<BookList>,
): String {
    val name = when (source) {
        RandomPickSource.WantToRead -> "Want to read"
        RandomPickSource.Owned -> "Owned"
        is RandomPickSource.UserList -> lists.firstOrNull { it.id == source.listId }?.name.orEmpty()
    }

    return "WHAT NEXT? · ${name.uppercase()}"
}
