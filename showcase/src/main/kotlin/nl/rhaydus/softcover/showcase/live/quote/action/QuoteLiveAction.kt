package nl.rhaydus.softcover.showcase.live.quote.action

import nl.rhaydus.softcover.showcase.live.quote.event.QuoteLiveEvent
import nl.rhaydus.softcover.showcase.live.quote.screenmodel.QuoteLiveDependencies
import nl.rhaydus.softcover.showcase.live.quote.state.QuoteLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.quote.state.QuoteLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface QuoteLiveAction : UiAction<
        QuoteLiveDependencies,
        QuoteLiveUiState,
        QuoteLiveEvent,
        QuoteLiveLocalVariables,
        >
