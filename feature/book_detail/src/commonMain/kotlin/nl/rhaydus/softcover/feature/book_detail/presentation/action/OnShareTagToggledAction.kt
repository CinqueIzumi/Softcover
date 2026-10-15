package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant
import nl.rhaydus.toad.ActionScope

internal class OnShareTagToggledAction(
    val variant: ShareCardVariant,
    val tag: String,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.setState { state ->
            when (variant) {
                ShareCardVariant.BOOK -> state.copy(
                    selectedBookShareTags = state.selectedBookShareTags.toggled(tag),
                )

                ShareCardVariant.MY_UPDATE -> state.copy(
                    deselectedUpdateShareTags = state.deselectedUpdateShareTags.toggled(tag),
                )
            }
        }
    }
}

private fun ImmutableSet<String>.toggled(tag: String): ImmutableSet<String> =
    (if (tag in this) this - tag else this + tag).toImmutableSet()
