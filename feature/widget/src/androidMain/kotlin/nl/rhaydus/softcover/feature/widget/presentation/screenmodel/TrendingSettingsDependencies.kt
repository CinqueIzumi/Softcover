package nl.rhaydus.softcover.feature.widget.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.widget.data.datastore.TrendingStore
import nl.rhaydus.softcover.feature.widget.presentation.worker.TrendingRefreshScheduler
import nl.rhaydus.toad.ActionDependencies

internal class TrendingSettingsDependencies(
    val store: TrendingStore,
    val scheduler: TrendingRefreshScheduler,
    val getThemeConfiguration: GetThemeConfigurationUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
