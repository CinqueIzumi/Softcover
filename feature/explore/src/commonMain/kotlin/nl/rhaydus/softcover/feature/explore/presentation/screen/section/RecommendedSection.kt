package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRecommendedMenuOpenAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_RECOMMENDED

@Composable
internal fun RecommendedSection(
    header: SectionHeaderUiModel,
    set: BookCardSet<Book>,
    visible: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
    trailingGap: Dp,
    rail: @Composable ((BookCardEvent) -> Unit) -> Unit,
) {
    val onCardEvent = rememberPrefetchingBookCardEventHandler(
        set = set,
    ) { event, book ->
        when (event) {
            is BookCardEvent.Clicked -> onBookClick(
                book,
                SURFACE_RECOMMENDED,
            )

            is BookCardEvent.MenuClicked -> {
                runAction(OnRecommendedMenuOpenAction(bookId = book.id))
            }

            is BookCardEvent.LongClicked,
            is BookCardEvent.PressStarted,
            is BookCardEvent.TrailingClicked,
            is BookCardEvent.ShakeEnded,
                -> Unit
        }
    }

    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(animationSpec = spatialSpec) + fadeIn(animationSpec = effectsSpec),
        exit = shrinkVertically(animationSpec = spatialSpec) + fadeOut(animationSpec = effectsSpec),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.padding(bottom = trailingGap),
        ) {
            SectionHeader(
                model = header,
                modifier = Modifier.padding(horizontal = Spacing.xl),
            )

            rail(onCardEvent)
        }
    }
}
