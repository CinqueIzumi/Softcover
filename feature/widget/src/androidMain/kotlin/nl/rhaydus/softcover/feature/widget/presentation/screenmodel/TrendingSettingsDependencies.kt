package nl.rhaydus.softcover.feature.widget.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetTrendingSettingsUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.SaveTrendingSettingsUseCase
import nl.rhaydus.softcover.feature.widget.presentation.glance.WidgetRepainter
import nl.rhaydus.softcover.feature.widget.presentation.worker.TrendingRefreshScheduler
import nl.rhaydus.toad.ActionDependencies

internal class TrendingSettingsDependencies(
    val getTrendingSettings: GetTrendingSettingsUseCase,
    val saveTrendingSettings: SaveTrendingSettingsUseCase,
    val repainter: WidgetRepainter,
    val scheduler: TrendingRefreshScheduler,
    val getThemeConfiguration: GetThemeConfigurationUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
