package nl.rhaydus.softcover.feature.widget.presentation.mapper

import android.net.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withTimeoutOrNull
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickResolution
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettingsChange
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSource
import nl.rhaydus.softcover.feature.widget.presentation.model.RandomPickWidgetUiModel
import kotlin.time.Duration.Companion.milliseconds

private class RandomPickWidgetInputs(
    val isSignedIn: Boolean,
    val themeConfiguration: ThemeConfiguration,
    val wantToRead: List<Book>,
    val lists: List<BookList>,
    val settingsChange: RandomPickSettingsChange?,
)

/**
 * A picked book emits its text first with the cover pending, then again with its image files. Files that already
 * exist resolve within [IMAGE_HIT_WINDOW] and skip the pending step. A failed resolve is logged and the last
 * model stays.
 *
 * Each [settingsChanges] emission, which arrives ahead of its matching [changes] one, first shows the loading
 * frame with the eyebrow for the new settings, then the resolved content. If that resolve fails, the model that
 * was showing before the loader returns, or the plain loading model when there was none, so the loader never
 * sticks. Every other input keeps the text-first behaviour and never shows the loader.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun randomPickWidgetModels(
    isSignedIn: Flow<Boolean>,
    themeConfiguration: Flow<ThemeConfiguration>,
    wantToRead: Flow<List<Book>>,
    lists: Flow<List<BookList>>,
    changes: Flow<Unit>,
    settingsChanges: Flow<RandomPickSettingsChange> = emptyFlow(),
    resolve: suspend (wantToRead: List<Book>, lists: List<BookList>) -> Result<RandomPickResolution>,
    coverUri: suspend (Book) -> Uri?,
    artwork: suspend (Book) -> RandomPickWidgetUiModel.Artwork?,
): Flow<RandomPickWidgetUiModel> = flow {
    var announced: RandomPickSettingsChange? = null
    var lastSettled: RandomPickWidgetUiModel? = null

    emitAll(
        combine(
            isSignedIn,
            themeConfiguration,
            wantToRead,
            lists,
            combine(
                changes,
                settingsChanges.map<RandomPickSettingsChange, RandomPickSettingsChange?> { it }.onStart { emit(null) },
            ) { _, settingsChange -> settingsChange },
        ) { signedIn, theme, books, userLists, settingsChange ->
            RandomPickWidgetInputs(
                isSignedIn = signedIn,
                themeConfiguration = theme,
                wantToRead = books,
                lists = userLists,
                settingsChange = settingsChange,
            )
        }.transformLatest { inputs ->
            val change = inputs.settingsChange
            val settled = FlowCollector<RandomPickWidgetUiModel> { model ->
                if (model.content !is RandomPickWidgetUiModel.Content.Switching) lastSettled = model

                emit(model)
            }

            var switching = false

            if (inputs.isSignedIn && change != null && change !== announced) {
                announced = change
                switching = true

                emit(
                    inputs.model(
                        RandomPickWidgetUiModel.Content.Switching(
                            eyebrow = eyebrowFor(
                                source = change.settings.source,
                                lists = inputs.lists,
                            ),
                        ),
                    ),
                )
            }

            val resolved = settled.resolveInto(
                inputs = inputs,
                resolve = resolve,
                coverUri = coverUri,
                artwork = artwork,
            )

            if (resolved.not() && switching) {
                emit(lastSettled ?: inputs.model(RandomPickWidgetUiModel.Content.Loading))
            }
        },
    )
}

/** Returns false when the resolve failed and nothing was emitted. */
private suspend fun FlowCollector<RandomPickWidgetUiModel>.resolveInto(
    inputs: RandomPickWidgetInputs,
    resolve: suspend (wantToRead: List<Book>, lists: List<BookList>) -> Result<RandomPickResolution>,
    coverUri: suspend (Book) -> Uri?,
    artwork: suspend (Book) -> RandomPickWidgetUiModel.Artwork?,
): Boolean {
    if (inputs.isSignedIn.not()) {
        emit(inputs.model(RandomPickWidgetUiModel.Content.SignedOut))
        return true
    }

    val resolution = resolve(
        inputs.wantToRead,
        inputs.lists,
    ).getOrElse { error ->
        AppLog.e(
            error,
            "Resolving the random pick failed",
        )

        return false
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

    return true
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
