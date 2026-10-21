package nl.rhaydus.softcover.showcase.live.quote.action

import nl.rhaydus.softcover.core.component.quote.QuotePrivacyOption
import nl.rhaydus.softcover.core.component.sheet.withModel
import nl.rhaydus.softcover.showcase.live.quote.event.QuoteLiveEvent
import nl.rhaydus.softcover.showcase.live.quote.mapper.withPrivacy
import nl.rhaydus.softcover.showcase.live.quote.screenmodel.QuoteLiveDependencies
import nl.rhaydus.softcover.showcase.live.quote.state.QuoteLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.quote.state.QuoteLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnQuotePrivacySelectedAction(val option: QuotePrivacyOption) : QuoteLiveAction {
    override suspend fun execute(
        dependencies: QuoteLiveDependencies,
        scope: ActionScope<QuoteLiveUiState, QuoteLiveEvent, QuoteLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = it.sheet.withModel { model -> model.withPrivacy(option = option) }) }
    }
}
