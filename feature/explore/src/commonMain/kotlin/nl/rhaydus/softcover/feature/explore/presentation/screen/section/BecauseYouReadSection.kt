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
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnBecauseYouReadGenreSelectedAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnBecauseYouReadGenreSheetVisibleChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.model.BecauseYouReadGenreControlEvent
import nl.rhaydus.softcover.feature.explore.presentation.model.BecauseYouReadGenreControlUiModel
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_BECAUSE_YOU_READ

@Composable
internal fun BecauseYouReadSection(
    header: SectionHeaderUiModel,
    genreControl: BecauseYouReadGenreControlUiModel,
    set: BookCardSet<Book>,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
    rail: @Composable ((BookCardEvent) -> Unit) -> Unit,
) {
    if (genreControl.isLoading.not() && (genreControl.visible.not() || set.isEmpty)) return

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
            model = genreControl,
            onEvent = { event ->
                runAction(
                    when (event) {
                        BecauseYouReadGenreControlEvent.Opened ->
                            OnBecauseYouReadGenreSheetVisibleChangeAction(visible = true)

                        BecauseYouReadGenreControlEvent.Dismissed ->
                            OnBecauseYouReadGenreSheetVisibleChangeAction(visible = false)

                        is BecauseYouReadGenreControlEvent.GenreSelected ->
                            OnBecauseYouReadGenreSelectedAction(genre = event.genre)
                    },
                )
            },
            modifier = Modifier.padding(horizontal = Spacing.xl),
        )

        rail(onCardEvent)
    }
}
