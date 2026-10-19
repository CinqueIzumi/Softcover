package nl.rhaydus.softcover.feature.widget.presentation.glance

import nl.rhaydus.softcover.core.book.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ShuffleRandomPickUseCase

internal class RandomPickWidgetDependencies(
    val environment: WidgetEnvironment,
    val repainter: WidgetRepainter,
    val getWantToReadUserBooks: GetWantToReadUserBooksUseCase,
    val getAllUserLists: GetAllUserListsUseCase,
    val shuffleRandomPick: ShuffleRandomPickUseCase,
)
