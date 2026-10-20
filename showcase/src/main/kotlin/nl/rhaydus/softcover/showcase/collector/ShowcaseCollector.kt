package nl.rhaydus.softcover.showcase.collector

import nl.rhaydus.softcover.showcase.event.ShowcaseEvent
import nl.rhaydus.softcover.showcase.screenmodel.ShowcaseDependencies
import nl.rhaydus.softcover.showcase.state.ShowcaseLocalVariables
import nl.rhaydus.softcover.showcase.state.ShowcaseUiState
import nl.rhaydus.toad.Collector

internal sealed interface ShowcaseCollector : Collector<
        ShowcaseUiState,
        ShowcaseEvent,
        ShowcaseDependencies,
        ShowcaseLocalVariables,
        >
