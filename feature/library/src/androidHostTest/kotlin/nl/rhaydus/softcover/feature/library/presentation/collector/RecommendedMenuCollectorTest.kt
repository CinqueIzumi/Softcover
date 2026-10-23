package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.isOpen
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.model.SystemVibe
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.recommendedBook
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.toRecommendedHeader
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecommendedMenuCollectorTest {
    private val header = SystemVibe.Recommendations.toRecommendedHeader(isLoading = false)

    private fun scopeFor(stateFlow: MutableStateFlow<LibraryUiState>) = ActionScope(
        stateFlow = stateFlow,
        localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
        eventChannel = Channel<LibraryEvent>(Channel.BUFFERED),
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `opens the sheet under the rail eyebrow with the requested book's title`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val stateFlow = MutableStateFlow(
                    LibraryUiState(
                        recommendedBooks = persistentListOf(
                            recommendedBook(id = 1),
                            recommendedBook(
                                id = 2,
                                title = "Dune",
                            ),
                        ),
                        recommendedHeader = header,
                        recommendedMenuBookId = 2,
                    ),
                )
                val job = launch {
                    RecommendedMenuCollector().onLaunch(
                        scope = scopeFor(stateFlow),
                        dependencies = mockk<LibraryDependencies>(relaxed = true),
                    )
                }

                // ----- Act -----
                val section = stateFlow.value.recommendedMenu.modelOrNull.shouldNotBeNull().scaffold.header
                    .shouldNotBeNull().section

                // ----- Assert -----
                section.eyebrow shouldBe "Not on your shelf yet"
                section.headlineText shouldBe "Dune"
                job.cancel()
            }

        @Test
        fun `stays closed while no book is requested`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val stateFlow = MutableStateFlow(
                LibraryUiState(
                    recommendedBooks = persistentListOf(recommendedBook(id = 1)),
                    recommendedHeader = header,
                ),
            )
            val job = launch {
                RecommendedMenuCollector().onLaunch(
                    scope = scopeFor(stateFlow),
                    dependencies = mockk<LibraryDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            val menu = stateFlow.value.recommendedMenu

            // ----- Assert -----
            menu shouldBe SheetSession.Closed
            job.cancel()
        }

        @Test
        fun `opens the sheet with the label's eyebrow when the header is a label`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val stateFlow = MutableStateFlow(
                    LibraryUiState(
                        recommendedBooks = persistentListOf(recommendedBook(id = 1)),
                        recommendedHeader = SectionHeaderUiModel.Label(eyebrow = "Not on your shelf yet"),
                        recommendedMenuBookId = 1,
                    ),
                )
                val job = launch {
                    RecommendedMenuCollector().onLaunch(
                        scope = scopeFor(stateFlow),
                        dependencies = mockk<LibraryDependencies>(relaxed = true),
                    )
                }

                // ----- Act -----
                val section = stateFlow.value.recommendedMenu.modelOrNull.shouldNotBeNull().scaffold.header
                    .shouldNotBeNull().section

                // ----- Assert -----
                section.eyebrow shouldBe "Not on your shelf yet"
                job.cancel()
            }

        @Test
        fun `updates the open sheet when the book's title changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dune = recommendedBook(
                id = 1,
                title = "Dune",
            )
            val stateFlow = MutableStateFlow(
                LibraryUiState(
                    recommendedBooks = persistentListOf(dune),
                    recommendedHeader = header,
                    recommendedMenuBookId = 1,
                ),
            )
            val job = launch {
                RecommendedMenuCollector().onLaunch(
                    scope = scopeFor(stateFlow),
                    dependencies = mockk<LibraryDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            val messiah = recommendedBook(
                id = 1,
                title = "Dune Messiah",
            )
            stateFlow.value = stateFlow.value.copy(recommendedBooks = persistentListOf(messiah))

            // ----- Assert -----
            val menu = stateFlow.value.recommendedMenu
            menu.isOpen shouldBe true
            menu.isClosing shouldBe false
            menu.modelOrNull.shouldNotBeNull().scaffold.header.shouldNotBeNull().section.headlineText shouldBe
                "Dune Messiah"
            job.cancel()
        }

        @Test
        fun `starts closing the sheet when the book leaves the rail`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val stateFlow = MutableStateFlow(
                LibraryUiState(
                    recommendedBooks = persistentListOf(recommendedBook(id = 1)),
                    recommendedHeader = header,
                    recommendedMenuBookId = 1,
                ),
            )
            val job = launch {
                RecommendedMenuCollector().onLaunch(
                    scope = scopeFor(stateFlow),
                    dependencies = mockk<LibraryDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(recommendedBooks = persistentListOf())

            // ----- Assert -----
            stateFlow.value.recommendedMenu.isClosing shouldBe true
            job.cancel()
        }

        @Test
        fun `starts closing the sheet when the requested book id is cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val stateFlow = MutableStateFlow(
                LibraryUiState(
                    recommendedBooks = persistentListOf(recommendedBook(id = 1)),
                    recommendedHeader = header,
                    recommendedMenuBookId = 1,
                ),
            )
            val job = launch {
                RecommendedMenuCollector().onLaunch(
                    scope = scopeFor(stateFlow),
                    dependencies = mockk<LibraryDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(recommendedMenuBookId = null)

            // ----- Assert -----
            stateFlow.value.recommendedMenu.isClosing shouldBe true
            job.cancel()
        }
    }
}
