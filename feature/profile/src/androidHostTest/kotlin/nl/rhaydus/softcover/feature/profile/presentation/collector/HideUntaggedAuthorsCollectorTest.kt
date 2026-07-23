package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetHideUntaggedAuthorsAsFlowUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope

class HideUntaggedAuthorsCollectorTest {
    private lateinit var getHideUntaggedAuthorsAsFlowUseCase: GetHideUntaggedAuthorsAsFlowUseCase
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var eventChannel: Channel<ProfileEvent>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>
    private lateinit var hideUntaggedAuthorsFlow: MutableSharedFlow<Boolean>

    @BeforeEach
    fun setUp() {
        hideUntaggedAuthorsFlow = MutableSharedFlow()
        getHideUntaggedAuthorsAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(ProfileUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = eventChannel,
        )

        every {
            getHideUntaggedAuthorsAsFlowUseCase()
        } returns hideUntaggedAuthorsFlow
    }

    private fun stubDependencies(testScope: TestScope): ProfileDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ProfileDependencies>(relaxed = true).also { mock ->
            every {
                mock.getHideUntaggedAuthorsAsFlowUseCase
            } returns getHideUntaggedAuthorsAsFlowUseCase

            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `updates state hideUntaggedAuthors to true when the preference emits true`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val collector = HideUntaggedAuthorsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            hideUntaggedAuthorsFlow.emit(true)

            // ----- Assert -----
            stateFlow.value.hideUntaggedAuthors shouldBe true
            job.cancel()
        }

        @Test
        fun `updates state hideUntaggedAuthors to false when the preference emits false`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(hideUntaggedAuthors = true)
            val dependencies = stubDependencies(this)
            val collector = HideUntaggedAuthorsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            hideUntaggedAuthorsFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.hideUntaggedAuthors shouldBe false
            job.cancel()
        }

        @Test
        fun `tracks the latest emission when the preference emits more than once`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val collector = HideUntaggedAuthorsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            hideUntaggedAuthorsFlow.emit(true)
            hideUntaggedAuthorsFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.hideUntaggedAuthors shouldBe false
            job.cancel()
        }
    }
}
