package nl.rhaydus.softcover.showcase.live.sheet.action

import kotlinx.coroutines.delay
import nl.rhaydus.softcover.core.component.sheet.open
import nl.rhaydus.softcover.core.component.sheet.withModel
import nl.rhaydus.softcover.showcase.live.sheet.event.SheetLiveEvent
import nl.rhaydus.softcover.showcase.live.sheet.mapper.sheetDemoModel
import nl.rhaydus.softcover.showcase.live.sheet.mapper.withProgress
import nl.rhaydus.softcover.showcase.live.sheet.model.SheetDemo
import nl.rhaydus.softcover.showcase.live.sheet.screenmodel.SheetLiveDependencies
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnDemoSelectedAction(val demo: SheetDemo) : SheetLiveAction {
    override suspend fun execute(
        dependencies: SheetLiveDependencies,
        scope: ActionScope<SheetLiveUiState, SheetLiveEvent, SheetLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = it.sheet.open(model = sheetDemoModel(demo = demo))) }

        if (demo != SheetDemo.LOADING) return

        repeat(LOADING_STEPS) { step ->
            delay(LOADING_STEP_MILLIS)

            val progress = (step + 1) / LOADING_STEPS.toFloat()

            scope.setState { state ->
                state.copy(sheet = state.sheet.withModel { it.withProgress(progress = progress) })
            }
        }
    }

    private companion object {
        const val LOADING_STEPS = 10
        const val LOADING_STEP_MILLIS = 300L
    }
}
