package nl.rhaydus.softcover.feature.widget.presentation.mapper

import android.graphics.Bitmap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickResolution
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSource
import nl.rhaydus.softcover.feature.widget.presentation.model.RandomPickWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropArtwork

private class RandomPickWidgetInputs(
    val isSignedIn: Boolean,
    val themeConfiguration: ThemeConfiguration,
    val wantToRead: List<Book>,
    val lists: List<BookList>,
)

@OptIn(ExperimentalCoroutinesApi::class)
internal fun randomPickWidgetModels(
    isSignedIn: Flow<Boolean>,
    themeConfiguration: Flow<ThemeConfiguration>,
    wantToRead: Flow<List<Book>>,
    lists: Flow<List<BookList>>,
    changes: Flow<Unit>,
    resolve: suspend (wantToRead: List<Book>, lists: List<BookList>) -> RandomPickResolution,
    loadCover: suspend (Book) -> Bitmap?,
    renderArtwork: suspend (cover: Bitmap?) -> BackdropArtwork?,
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
}.mapLatest { inputs ->
    RandomPickWidgetUiModel(
        themeConfiguration = inputs.themeConfiguration,
        content = inputs.toContent(
            resolve = resolve,
            loadCover = loadCover,
            renderArtwork = renderArtwork,
        ),
    )
}

private suspend fun RandomPickWidgetInputs.toContent(
    resolve: suspend (wantToRead: List<Book>, lists: List<BookList>) -> RandomPickResolution,
    loadCover: suspend (Book) -> Bitmap?,
    renderArtwork: suspend (cover: Bitmap?) -> BackdropArtwork?,
): RandomPickWidgetUiModel.Content {
    if (isSignedIn.not()) return RandomPickWidgetUiModel.Content.SignedOut

    val resolution = resolve(
        wantToRead,
        lists,
    )

    val eyebrow = eyebrowFor(
        source = resolution.source,
        lists = lists,
    )
    val book = resolution.book ?: return RandomPickWidgetUiModel.Content.EmptyPool(eyebrow = eyebrow)

    val cover = loadCover(book)

    return RandomPickWidgetUiModel.Content.Picked(
        eyebrow = eyebrow,
        bookId = book.id,
        title = book.title,
        author = book.authorString,
        cover = cover,
        artwork = renderArtwork(cover),
    )
}

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
