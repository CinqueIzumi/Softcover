package nl.rhaydus.softcover.showcase.live.quote.action

import nl.rhaydus.softcover.core.component.sheet.open
import nl.rhaydus.softcover.showcase.live.quote.event.QuoteLiveEvent
import nl.rhaydus.softcover.showcase.live.quote.mapper.quoteDemoModel
import nl.rhaydus.softcover.showcase.live.quote.model.QuoteDemo
import nl.rhaydus.softcover.showcase.live.quote.screenmodel.QuoteLiveDependencies
import nl.rhaydus.softcover.showcase.live.quote.state.QuoteLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.quote.state.QuoteLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnDemoSelectedAction(val demo: QuoteDemo) : QuoteLiveAction {
    override suspend fun execute(
        dependencies: QuoteLiveDependencies,
        scope: ActionScope<QuoteLiveUiState, QuoteLiveEvent, QuoteLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = it.sheet.open(model = quoteDemoModel(demo = demo))) }
    }
}
