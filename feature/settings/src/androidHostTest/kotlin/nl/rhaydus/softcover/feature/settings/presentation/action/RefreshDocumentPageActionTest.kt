package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.exception.OfflineException
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

private const val FALLBACK_MESSAGE = "fallback refresh failure"

class RefreshDocumentPageActionTest {
    private lateinit var source: DocumentPageSource
    private lateinit var dependencies: DocumentPageDependencies
    private lateinit var stateFlow: MutableStateFlow<DocumentPageUiState>
    private lateinit var scope: ActionScope<DocumentPageUiState, DocumentPageEvent, DocumentPageLocalVariables>

    @BeforeEach
    fun setUp() {
        source = mockk()
        stateFlow = MutableStateFlow(
            DocumentPage.WHATS_NEW.toInitialUiState().copy(refreshFailureMessage = FALLBACK_MESSAGE),
        )
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
    inner class Execute {
        @Test
        fun `shows the refreshing state with no error during the call and clears it afterwards`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(error = "stale error")
            var isRefreshingDuringCall = false
            var errorDuringCall: String? = "unset"

            coEvery {
                source.refresh(force = true)
            } coAnswers {
                isRefreshingDuringCall = scope.currentState.isRefreshing
                errorDuringCall = scope.currentState.error
                Result.success(Unit)
            }

            val action = RefreshDocumentPageAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            isRefreshingDuringCall shouldBe true
            errorDuringCall shouldBe null
            stateFlow.value.isRefreshing shouldBe false
            stateFlow.value.error shouldBe null
        }

        @Test
        fun `falls back to the source failure message when the error has no user message`() = runTest {
            // ----- Arrange -----
            coEvery {
                source.refresh(force = true)
            } returns Result.failure(RuntimeException("boom"))

            val action = RefreshDocumentPageAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.error shouldBe FALLBACK_MESSAGE
            stateFlow.value.isRefreshing shouldBe false
        }

        @Test
        fun `prefers the user message of a known error over the source fallback`() = runTest {
            // ----- Arrange -----
            coEvery {
                source.refresh(force = true)
            } returns Result.failure(OfflineException())

            val action = RefreshDocumentPageAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.error shouldBe "You're offline. Check your connection and try again."
            stateFlow.value.isRefreshing shouldBe false
        }

        @Test
        fun `does not dispatch a refresh when already refreshing`() = runTest {
            // ----- Arrange -----
            val initialState = stateFlow.value.copy(isRefreshing = true)
            stateFlow.value = initialState

            val action = RefreshDocumentPageAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { source.refresh(force = any()) }
            stateFlow.value shouldBe initialState
        }
    }
}
