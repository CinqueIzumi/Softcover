package nl.rhaydus.softcover.feature.settings.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.presentation.event.DocumentPageEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.toInitialUiState
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPage
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.DocumentPageDependencies
import nl.rhaydus.softcover.feature.settings.presentation.source.DocumentPageSource
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DocumentPageRefreshCollectorTest {
    private lateinit var source: DocumentPageSource
    private lateinit var dependencies: DocumentPageDependencies
    private lateinit var initialState: DocumentPageUiState
    private lateinit var stateFlow: MutableStateFlow<DocumentPageUiState>
    private lateinit var scope: ActionScope<DocumentPageUiState, DocumentPageEvent, DocumentPageLocalVariables>

    @BeforeEach
    fun setUp() {
        source = mockk()
        initialState = DocumentPage.ROADMAP.toInitialUiState()
        stateFlow = MutableStateFlow(initialState)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(DocumentPageLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<DocumentPageDependencies>(relaxed = true).also { mock ->
            every {
                mock.source
            } returns source
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `refreshes the source without bypassing the cache`() = runTest {
            // ----- Arrange -----
            coEvery {
                source.refresh(force = false)
            } returns Result.success(Unit)

            val collector = DocumentPageRefreshCollector()

            // ----- Act -----
            collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            )

            // ----- Assert -----
            coVerify { source.refresh(force = false) }
        }

        @Test
        fun `swallows a failed refresh without touching the error slot`() = runTest {
            // ----- Arrange -----
            coEvery {
                source.refresh(force = false)
            } returns Result.failure(RuntimeException("network error"))

            val collector = DocumentPageRefreshCollector()

            // ----- Act -----
            collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            )

            // ----- Assert -----
            stateFlow.value.error shouldBe null
            stateFlow.value shouldBe initialState
        }
    }
}
