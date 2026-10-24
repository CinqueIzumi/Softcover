package nl.rhaydus.softcover.feature.profile.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveBooksByIdsUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetHideUntaggedAuthorsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetHideUntaggedAuthorsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingLifeUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.toad.ActionDependencies

internal data class StatsAtlasDependencies(
    val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    val observeReadingLifeUseCase: ObserveReadingLifeUseCase,
    val getHideUntaggedAuthorsAsFlowUseCase: GetHideUntaggedAuthorsAsFlowUseCase,
    val setHideUntaggedAuthorsUseCase: SetHideUntaggedAuthorsUseCase,
    val observeBooksByIdsUseCase: ObserveBooksByIdsUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
