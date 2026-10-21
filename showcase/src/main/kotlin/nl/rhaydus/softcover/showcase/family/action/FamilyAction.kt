package nl.rhaydus.softcover.showcase.family.action

import nl.rhaydus.softcover.showcase.family.event.FamilyEvent
import nl.rhaydus.softcover.showcase.family.screenmodel.FamilyDependencies
import nl.rhaydus.softcover.showcase.family.state.FamilyLocalVariables
import nl.rhaydus.softcover.showcase.family.state.FamilyUiState
import nl.rhaydus.toad.UiAction

internal sealed interface FamilyAction : UiAction<
        FamilyDependencies,
        FamilyUiState,
        FamilyEvent,
        FamilyLocalVariables,
        >
