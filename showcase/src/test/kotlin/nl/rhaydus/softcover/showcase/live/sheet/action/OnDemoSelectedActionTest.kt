package nl.rhaydus.softcover.showcase.live.sheet.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.showcase.live.sheet.event.SheetLiveEvent
import nl.rhaydus.softcover.showcase.live.sheet.model.SheetDemo
import nl.rhaydus.softcover.showcase.live.sheet.model.SheetDemoUiModel
import nl.rhaydus.softcover.showcase.live.sheet.screenmodel.SheetLiveDependencies
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnDemoSelectedActionTest {
    private lateinit var dependencies: SheetLiveDependencies
    private lateinit var stateFlow: MutableStateFlow<SheetLiveUiState>
    private lateinit var scope: ActionScope<SheetLiveUiState, SheetLiveEvent, SheetLiveLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(SheetLiveUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SheetLiveLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `opens a scrolling sheet for the short demo`() = runTest {
            // ----- Arrange -----
            val action = OnDemoSelectedAction(demo = SheetDemo.SHORT)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.sheet.modelOrNull.shouldBeInstanceOf<SheetDemoUiModel.Scrolling>()
        }

        @Test
        fun `opens a pinned sheet for the long demo`() = runTest {
            // ----- Arrange -----
            val action = OnDemoSelectedAction(demo = SheetDemo.LONG)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.sheet.modelOrNull.shouldBeInstanceOf<SheetDemoUiModel.Pinned>()
        }

        @Test
        fun `opens a scrolling sheet for the error demo`() = runTest {
            // ----- Arrange -----
            val action = OnDemoSelectedAction(demo = SheetDemo.ERROR)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.sheet.modelOrNull.shouldBeInstanceOf<SheetDemoUiModel.Scrolling>()
        }

        @Test
        fun `opens the loading sheet at zero progress before any step`() = runTest {
            // ----- Arrange -----
            val action = OnDemoSelectedAction(demo = SheetDemo.LOADING)

            // ----- Act -----
            backgroundScope.launch {
                action.execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }
            runCurrent()

            // ----- Assert -----
            val loading = stateFlow.value.sheet.modelOrNull.shouldBeInstanceOf<SheetDemoUiModel.Loading>()
            loading.sheet.progress shouldBe 0f
        }

        @Test
        fun `steps the loading progress to half after half the wait`() = runTest {
            // ----- Arrange -----
            val action = OnDemoSelectedAction(demo = SheetDemo.LOADING)

            // ----- Act -----
            backgroundScope.launch {
                action.execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }
            advanceTimeBy(1_500)
            runCurrent()

            // ----- Assert -----
            val loading = stateFlow.value.sheet.modelOrNull.shouldBeInstanceOf<SheetDemoUiModel.Loading>()
            loading.sheet.progress shouldBe 0.5f
        }

        @Test
        fun `steps the loading progress up to one when the wait is over`() = runTest {
            // ----- Arrange -----
            val action = OnDemoSelectedAction(demo = SheetDemo.LOADING)

            // ----- Act -----
            backgroundScope.launch {
                action.execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }
            advanceTimeBy(3_000)
            runCurrent()

            // ----- Assert -----
            val loading = stateFlow.value.sheet.modelOrNull.shouldBeInstanceOf<SheetDemoUiModel.Loading>()
            loading.sheet.progress shouldBe 1f
        }
    }
}
