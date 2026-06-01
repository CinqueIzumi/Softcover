package nl.rhaydus.softcover.feature.library.presentation.flows

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DateStyleCollectorTest {

    private lateinit var getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>
    private lateinit var dateStyleFlow: MutableSharedFlow<DateStyle>

    @BeforeEach
    fun setUp() {
        dateStyleFlow = MutableSharedFlow()
        getDateStyleAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getDateStyleAsFlowUseCase()
        } returns dateStyleFlow

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getDateStyleAsFlowUseCase
            } returns getDateStyleAsFlowUseCase
        }
    }

    @Nested
    inner class OnLaunch {

        @Test
        fun `updates dateStyle when flow emits a value`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DateStyleCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            dateStyleFlow.emit(DateStyle.MONTH_DAY_YEAR)

            // ----- Assert -----
            stateFlow.value.dateStyle shouldBe DateStyle.MONTH_DAY_YEAR
            job.cancel()
        }

        @Test
        fun `updates dateStyle to the latest value when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DateStyleCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            dateStyleFlow.emit(DateStyle.DAY_MONTH_YEAR)
            dateStyleFlow.emit(DateStyle.YEAR_MONTH_DAY)

            // ----- Assert -----
            stateFlow.value.dateStyle shouldBe DateStyle.YEAR_MONTH_DAY
            job.cancel()
        }

        @Test
        fun `does not change dateStyle before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DateStyleCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act & Assert -----
            stateFlow.value.dateStyle shouldBe DateStyle.DAY_MONTH_YEAR
            job.cancel()
        }

        @Test
        fun `retains the last emitted DateStyle after the collector job is cancelled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DateStyleCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }
            dateStyleFlow.emit(DateStyle.MONTH_DAY_YEAR)
            job.cancel()

            // ----- Act & Assert -----
            stateFlow.value.dateStyle shouldBe DateStyle.MONTH_DAY_YEAR
        }

        @Test
        fun `preserves all other state fields when updating dateStyle`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isLoading = false,
                isLayoutMenuExpanded = true,
                dateStyle = DateStyle.DAY_MONTH_YEAR,
            )
            val collector = DateStyleCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            dateStyleFlow.emit(DateStyle.MONTH_DAY_YEAR)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.isLayoutMenuExpanded shouldBe true
            stateFlow.value.dateStyle shouldBe DateStyle.MONTH_DAY_YEAR
            job.cancel()
        }
    }
}
