package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.recommendedMenuSheetFor
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.softcover.feature.explore.presentation.state.PendingRecommendedHide
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecommendedMenuCollectorTest {
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    private val book = PreviewData.baseBook.copy(id = 5)

    private val eyebrow = "Recommended for you"

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stateWith(
        books: List<Book> = listOf(book),
        requestedBookId: Int? = null,
        header: SectionHeaderUiModel = SectionHeaderUiModel.Section(
            eyebrow = eyebrow,
            headline = "Based on what you've read",
        ),
    ): ExploreScreenUiState = ExploreScreenUiState(
        recommendedBooks = books.toImmutableList(),
        recommendedMenuBookId = requestedBookId,
        recommendedHeader = header,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `opens the menu with the section eyebrow and the book title`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                RecommendedMenuCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateWith(requestedBookId = 5)

            // ----- Assert -----
            val header = stateFlow.value.recommendedMenu.modelOrNull.shouldNotBeNull().scaffold.header.shouldNotBeNull()
            header.section.eyebrow shouldBe eyebrow
            header.section.headlineText shouldBe book.title
            job.cancel()
        }

        @Test
        fun `uses the eyebrow of the vibe the section currently shows`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                RecommendedMenuCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateWith(
                requestedBookId = 5,
                header = SectionHeaderUiModel.Section(
                    eyebrow = "Top picks",
                    headline = "Popular with readers right now",
                ),
            )

            // ----- Assert -----
            val header = stateFlow.value.recommendedMenu.modelOrNull.shouldNotBeNull().scaffold.header.shouldNotBeNull()
            header.section.eyebrow shouldBe "Top picks"
            job.cancel()
        }

        @Test
        fun `keeps the session closed when nothing is requested`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                RecommendedMenuCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateWith(requestedBookId = null)

            // ----- Assert -----
            stateFlow.value.recommendedMenu shouldBe SheetSession.Closed
            job.cancel()
        }

        @Test
        fun `opens the menu with the inline header's eyebrow`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                RecommendedMenuCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateWith(
                requestedBookId = 5,
                header = SectionHeaderUiModel.Inline(eyebrow = "Inline eyebrow"),
            )

            // ----- Assert -----
            val header = stateFlow.value.recommendedMenu.modelOrNull.shouldNotBeNull().scaffold.header.shouldNotBeNull()
            header.section.eyebrow shouldBe "Inline eyebrow"
            job.cancel()
        }

        @Test
        fun `requests close and keeps the pending hide when the book leaves the recommended list`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    RecommendedMenuCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                stateFlow.value = stateWith(requestedBookId = 5)
                val pending = PendingRecommendedHide(bookId = 5)
                stateFlow.value = stateFlow.value.copy(
                    recommendedMenu = SheetSession.Open(
                        model = recommendedMenuSheetFor(
                            book = book,
                            eyebrow = eyebrow,
                        ),
                        pending = pending,
                    ),
                )

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(recommendedBooks = persistentListOf())

                // ----- Assert -----
                stateFlow.value.recommendedMenu.isClosing shouldBe true
                stateFlow.value.recommendedMenu.dismissed() shouldBe pending
                job.cancel()
            }

        @Test
        fun `requests close and keeps the pending hide when the request clears`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                RecommendedMenuCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateWith(requestedBookId = 5)
            val pending = PendingRecommendedHide(bookId = 5)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                recommendedMenu = SheetSession.Open(
                    model = recommendedMenuSheetFor(
                        book = book,
                        eyebrow = eyebrow,
                    ),
                    pending = pending,
                ),
                recommendedMenuBookId = null,
            )

            // ----- Assert -----
            stateFlow.value.recommendedMenu.isClosing shouldBe true
            stateFlow.value.recommendedMenu.dismissed() shouldBe pending
            job.cancel()
        }

        @Test
        fun `does not reopen once the request has been cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                RecommendedMenuCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateWith(requestedBookId = 5)
            stateFlow.value = stateFlow.value.copy(recommendedMenuBookId = null)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(recommendedBooks = persistentListOf(book.copy(title = "Updated")))

            // ----- Assert -----
            stateFlow.value.recommendedMenu.isClosing shouldBe true
            stateFlow.value.recommendedMenu.dismissed().shouldBeNull()
            job.cancel()
        }
    }
}
