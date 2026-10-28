package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.profile.presentation.action.OnDayDetailBookTappedAction
import nl.rhaydus.softcover.feature.profile.presentation.action.OnDayDetailSheetDismissedAction
import nl.rhaydus.softcover.feature.profile.presentation.action.StatsAtlasAction
import nl.rhaydus.softcover.feature.profile.presentation.state.DayDetailBook
import nl.rhaydus.softcover.feature.profile.presentation.state.DayDetailUiModel

/** Hosts the tapped reading day's sheet. Rendered beside the scrolling body, never inside it. */
@Composable
internal fun DayDetailSheet(
    session: SheetSession<DayDetailUiModel, DayDetailBook>,
    runAction: (StatsAtlasAction) -> Unit,
) {
    session.modelOrNull?.let { model ->
        val onBookCardEvent = rememberPrefetchingBookCardEventHandler(set = model.books) { event, book ->
            if (event is BookCardEvent.Clicked) {
                runAction(OnDayDetailBookTappedAction(book = book))
            }
        }

        SheetScaffold(
            model = model.scaffold,
            onEvent = { event ->
                when (event) {
                    SheetScaffoldEvent.Dismissed -> runAction(OnDayDetailSheetDismissedAction())

                    SheetScaffoldEvent.PrimaryClicked,
                    SheetScaffoldEvent.SecondaryClicked,
                    SheetScaffoldEvent.HeaderActionClicked -> Unit
                }
            },
            content = {
                DayDetailBody(
                    model = model,
                    onBookCardEvent = onBookCardEvent,
                )
            },
        )
    }
}

@Composable
private fun DayDetailBody(
    model: DayDetailUiModel,
    onBookCardEvent: (BookCardEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        model.books.cards.forEach { card ->
            BookCard(
                model = card,
                onEvent = onBookCardEvent,
            )
        }
    }
}

@StandardPreview
@Composable
private fun DayDetailBodyPreview() {
    SoftcoverTheme(brightness = SchemeBrightness.LIGHT) {
        Column {
            DayDetailUiModel.previews.values.forEach { model ->
                DayDetailBody(
                    model = model,
                    onBookCardEvent = {},
                )
            }
        }
    }
}
