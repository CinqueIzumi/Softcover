package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_BECAUSE_YOU_READ

@Composable
internal fun BecauseYouReadSection(
    genre: String?,
    header: SectionHeaderUiModel,
    genreOptions: List<String>,
    genreSheet: SheetScaffoldUiModel,
    genreGroup: SheetRowGroupUiModel<String?>,
    set: BookCardSet<Book>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
    rail: @Composable ((BookCardEvent) -> Unit) -> Unit,
) {
    if (isLoading.not() && (genre == null || set.isEmpty)) return

    val onCardEvent = rememberPrefetchingBookCardEventHandler(
        set = set,
    ) { event, book ->
        if (event is BookCardEvent.Clicked) {
            onBookClick(
                book,
                SURFACE_BECAUSE_YOU_READ,
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionHeader(
            model = header,
            modifier = Modifier.padding(horizontal = Spacing.xl),
        )

        BecauseYouReadGenreControl(
            genre = genre ?: "Fantasy",
            options = genreOptions,
            sheet = genreSheet,
            genreGroup = genreGroup,
            runAction = runAction,
            isLoading = isLoading,
            modifier = Modifier.padding(horizontal = Spacing.xl),
        )

        rail(onCardEvent)
    }
}
