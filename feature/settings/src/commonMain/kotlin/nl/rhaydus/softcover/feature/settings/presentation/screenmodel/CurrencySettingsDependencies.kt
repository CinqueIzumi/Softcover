package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetCurrenciesUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetUsualCurrencyAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetUsualCurrencyUseCase
import nl.rhaydus.toad.ActionDependencies

internal class CurrencySettingsDependencies(
    val getCurrenciesUseCase: GetCurrenciesUseCase,
    val getUsualCurrencyAsFlowUseCase: GetUsualCurrencyAsFlowUseCase,
    val setUsualCurrencyUseCase: SetUsualCurrencyUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
