package nl.rhaydus.softcover.feature.widget.settings.trending

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.widget.trending.TrendingStore
import nl.rhaydus.softcover.feature.widget.worker.TrendingRefreshScheduler
import nl.rhaydus.toad.ActionDependencies

internal class TrendingSettingsDependencies(
    val store: TrendingStore,
    val scheduler: TrendingRefreshScheduler,
    val getThemeConfiguration: GetThemeConfigurationUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
