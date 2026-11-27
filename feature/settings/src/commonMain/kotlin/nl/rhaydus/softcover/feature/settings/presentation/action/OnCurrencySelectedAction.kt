package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.settings.presentation.event.CurrencySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.CurrencySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsUiState
import nl.rhaydus.toad.ActionScope

internal class OnCurrencySelectedAction(val code: String) : CurrencySettingsAction {
    override suspend fun execute(
        dependencies: CurrencySettingsDependencies,
        scope: ActionScope<CurrencySettingsUiState, CurrencySettingsEvent, CurrencySettingsLocalVariables>,
    ) {
        dependencies.setUsualCurrencyUseCase(code = code).onFailure {
            AppLog.e("$it")
        }
    }
}
