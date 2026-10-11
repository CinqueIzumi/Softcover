package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.bookcard.rememberBookCardEventHandler
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.explore.presentation.action.HiddenSuggestionsAction
import nl.rhaydus.softcover.feature.explore.presentation.model.HiddenGroupHeaderUiModel
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState

/**
 * The Hidden-suggestions body, shared by the mobile [HiddenSuggestionsScreen] page and the desktop
 * standalone layout. An intro line opens the surface (the screen's title lives in the top bar, matching
 * the sibling Appearance / Library-tabs settings pages), then two hairline-separated groups — "Hidden
 * books" / "Hidden series" — each list their rows with a per-item warm "Restore" affordance; a group is
 * omitted entirely when its list is empty. It does not render the all-empty state
 * ([HiddenSuggestionsUiState.isEmpty]): the screen layouts show that outside the scroll, where it can
 * centre. The caller supplies the scroll / width [modifier].
 */
@Composable
internal fun HiddenSuggestionsContent(
    state: HiddenSuggestionsUiState,
    runAction: (HiddenSuggestionsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SoftcoverText(
            text = state.intro,
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Spacing.xxxl))

        val hasBooksGroup = state.isLoadingBooks || state.hiddenBooks.isEmpty.not()

        if (hasBooksGroup) {
            HiddenGroupSection(
                header = state.hiddenBooksGroupHeader,
                set = state.hiddenBooks,
                isLoading = state.isLoadingBooks,
                runAction = runAction,
            )
        }

        if (state.isLoadingSeries || state.hiddenSeries.isEmpty.not()) {
            if (hasBooksGroup) {
                Spacer(modifier = Modifier.height(Spacing.section))
            }

            HiddenGroupSection(
                header = state.hiddenSeriesGroupHeader,
                set = state.hiddenSeries,
                isLoading = state.isLoadingSeries,
                runAction = runAction,
            )
        }
    }
}

@Composable
private fun HiddenGroupSection(
    header: HiddenGroupHeaderUiModel,
    set: BookCardSet<HiddenSuggestionsAction>,
    isLoading: Boolean,
    runAction: (HiddenSuggestionsAction) -> Unit,
) {
    val onEvent = rememberBookCardEventHandler(
        set = set,
    ) { event, action ->
        if (event is BookCardEvent.Clicked) {
            runAction(action)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HiddenSuggestionsGroupHeader(
            model = header,
            isLoading = isLoading,
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        set.cards.forEach { card ->
            key(card.key.lazyKey) {
                BookCard(
                    model = card,
                    onEvent = onEvent,
                )
            }
        }
    }
}

/**
 * The compact header opening each group within the page: [SectionHeader]'s [SectionHeaderUiModel.Inline]
 * register, with a demoted tabular-figure count trailing the eyebrow and a one-line italic subline
 * standing in for a headline (the screen's own title lives in the top bar). "Hidden books" / "Hidden
 * series" are the only two consumers today, so this stays screen-local; promote it to a shared component
 * only once a second screen needs the same "nested group inside an already-opened page" header shape.
 */
@Composable
private fun HiddenSuggestionsGroupHeader(
    model: HiddenGroupHeaderUiModel,
    isLoading: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(model = model.header) {
            SoftcoverText(
                text = model.count,
                isLoading = isLoading,
                standIn = "00",
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        SoftcoverText(
            text = model.subline,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
