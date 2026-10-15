package nl.rhaydus.softcover.feature.widget.currentlyreading

import android.graphics.Bitmap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.uibinding.format.percentLabel
import nl.rhaydus.softcover.feature.widget.backdrop.BackdropArtwork
import nl.rhaydus.softcover.feature.widget.progress.WavyProgressBitmaps

private class WidgetInputs(
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
    renderProgress: (fraction: Float) -> WavyProgressBitmaps,
    renderArtwork: suspend (cover: Bitmap?) -> BackdropArtwork? = { null },
): Flow<CurrentlyReadingWidgetUiModel> = combine(
    isSignedIn,
    themeConfiguration,
    shelf,
    selectedBookId,
    ::WidgetInputs,
).mapLatest { inputs ->
    CurrentlyReadingWidgetUiModel(
        themeConfiguration = inputs.themeConfiguration,
        content = inputs.toContent(
            loadCover = loadCover,
            renderProgress = renderProgress,
            renderArtwork = renderArtwork,
        ),
    )
}

private suspend fun WidgetInputs.toContent(
    loadCover: suspend (Book) -> Bitmap?,
    renderProgress: (fraction: Float) -> WavyProgressBitmaps,
    renderArtwork: suspend (cover: Bitmap?) -> BackdropArtwork?,
): CurrentlyReadingWidgetUiModel.Content {
    if (isSignedIn.not()) return CurrentlyReadingWidgetUiModel.Content.SignedOut

    val orderedBookIds = books.map { it.id }

    val shownId = resolveShownBookId(
        storedBookId = selectedBookId,
        orderedBookIds = orderedBookIds,
    ) ?: return CurrentlyReadingWidgetUiModel.Content.Empty

    val book = books.first { it.id == shownId }
    val fraction = (book.currentRead?.progress ?: 0f) / 100f

    val cover = loadCover(book)

    return CurrentlyReadingWidgetUiModel.Content.Reading(
        title = book.title,
        author = book.authorString,
        progressLabel = percentLabel(fraction = fraction),
        cover = cover,
        progress = renderProgress(fraction),
        position = if (books.size > 1) "${orderedBookIds.indexOf(shownId) + 1}/${books.size}" else null,
        artwork = renderArtwork(cover),
    )
}
