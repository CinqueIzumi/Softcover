package nl.rhaydus.softcover.feature.widget.presentation.mapper

import android.graphics.Bitmap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.uibinding.format.percentLabel
import nl.rhaydus.softcover.feature.widget.presentation.model.CurrentlyReadingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.model.ProgressBarKey
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropArtwork
import nl.rhaydus.softcover.feature.widget.presentation.render.WavyProgressBitmaps
import nl.rhaydus.softcover.feature.widget.presentation.util.resolveShownBookId

private class CurrentlyReadingWidgetInputs(
    val isSignedIn: Boolean,
    val themeConfiguration: ThemeConfiguration,
    val books: List<Book>,
    val selectedBookId: Int?,
)

/** [shelf] arrives ordered most recently updated first. */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun currentlyReadingWidgetModels(
    isSignedIn: Flow<Boolean>,
    themeConfiguration: Flow<ThemeConfiguration>,
    shelf: Flow<List<Book>>,
    selectedBookId: Flow<Int?>,
    loadCover: suspend (Book) -> Bitmap?,
    barWidthsDp: List<Int>,
    renderProgress: (fraction: Float, widthDp: Int) -> WavyProgressBitmaps,
    renderArtwork: suspend (cover: Bitmap?) -> BackdropArtwork? = { null },
): Flow<CurrentlyReadingWidgetUiModel> {
    // Only the `mapLatest` transform touches this, so it needs no synchronisation.
    val barCache = mutableMapOf<ProgressBarKey, WavyProgressBitmaps>()

    return combine(
        isSignedIn,
        themeConfiguration,
        shelf,
        selectedBookId,
        ::CurrentlyReadingWidgetInputs,
    ).mapLatest { inputs ->
        CurrentlyReadingWidgetUiModel(
            themeConfiguration = inputs.themeConfiguration,
            content = inputs.toContent(
                loadCover = loadCover,
                barWidthsDp = barWidthsDp,
                renderProgress = renderProgress,
                renderArtwork = renderArtwork,
                barCache = barCache,
            ),
        )
    }
}

private suspend fun CurrentlyReadingWidgetInputs.toContent(
    loadCover: suspend (Book) -> Bitmap?,
    barWidthsDp: List<Int>,
    renderProgress: (fraction: Float, widthDp: Int) -> WavyProgressBitmaps,
    renderArtwork: suspend (cover: Bitmap?) -> BackdropArtwork?,
    barCache: MutableMap<ProgressBarKey, WavyProgressBitmaps>,
): CurrentlyReadingWidgetUiModel.Content {
    if (isSignedIn.not()) return CurrentlyReadingWidgetUiModel.Content.SignedOut

    val orderedBookIds = books.map { it.id }

    val shownId = resolveShownBookId(
        storedBookId = selectedBookId,
        orderedBookIds = orderedBookIds,
    ) ?: return CurrentlyReadingWidgetUiModel.Content.Empty

    val book = books.first { it.id == shownId }
    val fraction = (book.currentRead?.progress ?: 0f) / 100f

    barCache.keys.removeAll { it.bookId !in orderedBookIds || (it.bookId == book.id && it.fraction != fraction) }

    val cover = loadCover(book)

    return CurrentlyReadingWidgetUiModel.Content.Reading(
        bookId = book.id,
        title = book.title,
        author = book.authorString,
        progressLabel = percentLabel(fraction = fraction),
        cover = cover,
        progress = barWidthsDp.associateWith { widthDp ->
            barCache.getOrPut(
                ProgressBarKey(
                    bookId = book.id,
                    fraction = fraction,
                    widthDp = widthDp,
                ),
            ) {
                renderProgress(
                    fraction,
                    widthDp,
                )
            }
        },
        position = if (books.size > 1) "${orderedBookIds.indexOf(shownId) + 1}/${books.size}" else null,
        artwork = renderArtwork(cover),
    )
}
