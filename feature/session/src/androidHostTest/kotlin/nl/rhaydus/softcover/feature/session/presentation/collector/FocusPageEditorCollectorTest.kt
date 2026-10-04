package nl.rhaydus.softcover.feature.session.presentation.collector

import androidx.compose.ui.text.input.TextFieldValue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.feature.session.presentation.event.FocusModeEvent
import nl.rhaydus.softcover.feature.session.presentation.screenmodel.FocusModeDependencies
import nl.rhaydus.softcover.feature.session.presentation.state.FocusModeUiState
import nl.rhaydus.softcover.feature.session.presentation.state.LocalFocusModeVariables
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FocusPageEditorCollectorTest {
    private lateinit var activeSessionFlow: MutableStateFlow<ActiveSession?>
    private lateinit var dependencies: FocusModeDependencies
    private lateinit var stateFlow: MutableStateFlow<FocusModeUiState>
    private lateinit var scope: ActionScope<FocusModeUiState, FocusModeEvent, LocalFocusModeVariables>

    @BeforeEach
    fun setUp() {
        activeSessionFlow = MutableStateFlow(null)
        val controller = mockk<ActiveSessionController>(relaxed = true) {
            every {
                activeSession
            } returns activeSessionFlow
        }
        dependencies = mockk(relaxed = true) {
            every {
                activeSessionController
            } returns controller
        }
        stateFlow = MutableStateFlow(FocusModeUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalFocusModeVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubSession(
        totalPages: Int?,
        currentPage: Int?,
    ): ActiveSession = mockk(relaxed = true) {
        every {
            book
        } returns mockk(relaxed = true) {
            every {
                currentEdition
            } returns mockk(relaxed = true) {
                every {
                    pages
                } returns totalPages
            }
            every {
                userBookRead
            } returns mockk(relaxed = true) {
                every {
                    this@mockk.currentPage
                } returns currentPage
            }
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `seeds the draft from the stored page and builds the editor`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            activeSessionFlow.value = stubSession(
                totalPages = 200,
                currentPage = 50,
            )
            val collector = FocusPageEditorCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.pageDraft.text shouldBe "50"
            val editor = stateFlow.value.pageEditor.shouldNotBeNull()
            editor.caption shouldBe "of 200 pages"
            editor.fraction shouldBe 0.25f
            job.cancel()
        }

        @Test
        fun `seeds zero when the stored page is missing`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            activeSessionFlow.value = stubSession(
                totalPages = 200,
                currentPage = null,
            )
            val collector = FocusPageEditorCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.pageDraft.text shouldBe "0"
            job.cancel()
        }

        @Test
        fun `leaves the editor null until an active session exists`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = FocusPageEditorCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.pageEditor shouldBe null
            job.cancel()
        }

        @Test
        fun `rebuilds the bar when the draft changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            activeSessionFlow.value = stubSession(
                totalPages = 200,
                currentPage = 50,
            )
            val collector = FocusPageEditorCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(pageDraft = TextFieldValue(text = "100"))

            // ----- Assert -----
            stateFlow.value.pageEditor.shouldNotBeNull().fraction shouldBe 0.5f
            job.cancel()
        }

        @Test
        fun `rebuilds the editor when the active session changes without overwriting the draft`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                activeSessionFlow.value = stubSession(
                    totalPages = 200,
                    currentPage = 50,
                )
                val collector = FocusPageEditorCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                stateFlow.value = stateFlow.value.copy(pageDraft = TextFieldValue(text = "60"))

                // ----- Act -----
                activeSessionFlow.value = stubSession(
                    totalPages = 400,
                    currentPage = 70,
                )

                // ----- Assert -----
                stateFlow.value.pageDraft.text shouldBe "60"
                stateFlow.value.pageEditor.shouldNotBeNull().caption shouldBe "of 400 pages"
                job.cancel()
            }
    }
}
