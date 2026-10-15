package nl.rhaydus.softcover.feature.widget.randompick

import nl.rhaydus.softcover.core.book.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.presentation.navigation.AppEntryPoint

internal class RandomPickWidgetDependencies(
    val getUserIdAsFlow: GetUserIdAsFlowUseCase,
    val getThemeConfiguration: GetThemeConfigurationUseCase,
    val getWantToReadUserBooks: GetWantToReadUserBooksUseCase,
    val getAllUserLists: GetAllUserListsUseCase,
    val store: RandomPickStore,
    val appEntryPoint: AppEntryPoint,
)
