package nl.rhaydus.softcover.feature.widget.presentation.mapper

import android.net.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.uibinding.format.percentLabel
import nl.rhaydus.softcover.feature.widget.domain.util.resolveShownBookId
import nl.rhaydus.softcover.feature.widget.presentation.model.BookArtworkUiModel
import nl.rhaydus.softcover.feature.widget.presentation.model.CurrentlyReadingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.WavyProgressUris

private class CurrentlyReadingWidgetInputs(
    val isSignedIn: Boolean,
    val themeConfiguration: ThemeConfiguration,
    val books: List<Book>,
    val selectedBookId: Int?,
)

/**
 * Emits each model for a shown book first with its text and no images, then with the stored image [Uri]s. The
 * cover is rendered again only when [coverHeightDp] changes and the bars whenever [barWidthsDp] changes, so the
 * text never waits on rendering. [shelf] arrives ordered most recently updated first.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun currentlyReadingWidgetModels(
    isSignedIn: Flow<Boolean>,
    themeConfiguration: Flow<ThemeConfiguration>,
    shelf: Flow<List<Book>>,
    selectedBookId: Flow<Int?>,
    coverUri: suspend (book: Book, coverHeightDp: Int) -> Uri?,
    coverHeightDp: Flow<Int>,
    barWidthsDp: Flow<Set<Int>>,
    progressUris: suspend (fraction: Float, widthDp: Int) -> WavyProgressUris?,
    artworkUris: suspend (Book) -> BookArtworkUiModel? = { null },
): Flow<CurrentlyReadingWidgetUiModel> = combine(
    isSignedIn,
    themeConfiguration,
    shelf,
    selectedBookId,
    ::CurrentlyReadingWidgetInputs,
).transformLatest { inputs ->
    val textContent = inputs.toTextContent()

    emit(
        CurrentlyReadingWidgetUiModel(
            themeConfiguration = inputs.themeConfiguration,
            content = textContent,
        ),
    )

    if (textContent is CurrentlyReadingWidgetUiModel.Content.Reading) {
        val book = inputs.books.first { it.id == textContent.bookId }

        val artwork = artworkUris(book)

        coverHeightDp.collectLatest { heightDp ->
            val cover = coverUri(
                book,
                heightDp,
            )

            barWidthsDp.collectLatest { widths ->
                val progress = widths.mapNotNull { widthDp ->
                    progressUris(
                        book.progressFraction(),
                        widthDp,
                    )?.let { widthDp to it }
                }.toMap()

                emit(
                    CurrentlyReadingWidgetUiModel(
                        themeConfiguration = inputs.themeConfiguration,
                        content = textContent.copy(
                            cover = cover,
                            progress = progress,
                            artwork = artwork,
                            imagesPending = false,
                        ),
                    ),
                )
            }
        }
    }
}

private fun CurrentlyReadingWidgetInputs.toTextContent(): CurrentlyReadingWidgetUiModel.Content {
    if (isSignedIn.not()) return CurrentlyReadingWidgetUiModel.Content.SignedOut

    val orderedBookIds = books.map { it.id }

    val shownId = resolveShownBookId(
        storedBookId = selectedBookId,
        orderedBookIds = orderedBookIds,
    ) ?: return CurrentlyReadingWidgetUiModel.Content.Empty

    val book = books.first { it.id == shownId }

    return CurrentlyReadingWidgetUiModel.Content.Reading(
        bookId = book.id,
        title = book.displayTitle,
        author = book.authorString,
        progressLabel = percentLabel(fraction = book.progressFraction()),
        cover = null,
        progress = emptyMap(),
        eyebrow = if (books.size > 1) {
            "READING · ${orderedBookIds.indexOf(shownId) + 1}/${books.size}"
        } else {
            "READING"
        },
        canCycle = books.size > 1,
        artwork = null,
        imagesPending = true,
    )
}

private fun Book.progressFraction(): Float = (currentRead?.progress ?: 0f) / 100f
