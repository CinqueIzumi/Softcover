package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.profile.domain.usecase.FillReaderEditionLanguagesUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EditionLanguagesCollectorTest {
    private lateinit var fillReaderEditionLanguagesUseCase: FillReaderEditionLanguagesUseCase
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>

    @BeforeEach
    fun setUp() {
        fillReaderEditionLanguagesUseCase = mockk()
        stateFlow = MutableStateFlow(StatsAtlasUiState())

        dependencies = mockk<StatsAtlasDependencies>(relaxed = true)

        every {
            dependencies.fillReaderEditionLanguagesUseCase
        } returns fillReaderEditionLanguagesUseCase

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `stays pending while the fill runs and clears once it completes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val gate = CompletableDeferred<Unit>()

            coEvery {
                fillReaderEditionLanguagesUseCase()
            } coAnswers {
                gate.await()
                Result.success(Unit)
            }

            val job = launch {
                EditionLanguagesCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            val whileRunning = stateFlow.value.editionLanguagesPending

            // ----- Act -----
            gate.complete(Unit)

            // ----- Assert -----
            whileRunning shouldBe true
            stateFlow.value.editionLanguagesPending shouldBe false
            job.cancel()
        }

        @Test
        fun `clears the pending flag when the fill fails`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                fillReaderEditionLanguagesUseCase()
            } returns Result.failure(IllegalStateException("offline"))

            // ----- Act -----
            val job = launch {
                EditionLanguagesCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.editionLanguagesPending shouldBe false
            job.cancel()
        }
    }
}
