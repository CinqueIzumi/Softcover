package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import nl.rhaydus.common.currentLocalDate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.uibinding.date.formatLongDate
import nl.rhaydus.softcover.core.uibinding.release.toEditionUnreleasedBadgeUiModel
import nl.rhaydus.softcover.core.uibinding.release.toProminentUnreleasedBadgeUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private fun daysFromToday(days: Int): LocalDate = currentLocalDate().plus(DatePeriod(days = days))

class UnreleasedBadgeCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    private fun stubBook(releaseDate: LocalDate?): Book = mockk {
        every {
            this@mockk.releaseDate
        } returns releaseDate

        every {
            userBook
        } returns null

        every {
            currentEdition
        } returns null
    }

    private fun editionReleasing(date: LocalDate?): BookEdition = buildEdition(id = 1).copy(releaseDate = date)

    private fun TestScope.launchCollector(): Job = launch {
        UnreleasedBadgeCollector().onLaunch(
            scope = scope,
            dependencies = dependencies,
        )
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets a Prominent badge and no caption when the book is in the future and no edition is shown`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val bookDate = daysFromToday(30)
                stateFlow.value = buildBookDetailUiState().copy(book = stubBook(bookDate))

                // ----- Act -----
                val job = launchCollector()

                // ----- Assert -----
                stateFlow.value.unreleasedBadge shouldBe bookDate.toProminentUnreleasedBadgeUiModel()
                stateFlow.value.releaseCaption shouldBe null
                job.cancel()
            }

        @Test
        fun `clears badge and caption for a released book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(daysFromToday(-30)))

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            stateFlow.value.unreleasedBadge shouldBe null
            stateFlow.value.releaseCaption shouldBe null
            job.cancel()
        }

        @Test
        fun `clears badge and caption when the book has no release date`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(null))

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            stateFlow.value.unreleasedBadge shouldBe null
            stateFlow.value.releaseCaption shouldBe null
            job.cancel()
        }

        @Test
        fun `clears badge and caption when state book is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = null)

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            stateFlow.value.unreleasedBadge shouldBe null
            stateFlow.value.releaseCaption shouldBe null
            job.cancel()
        }

        @Test
        fun `shows the edition badge with a First published caption when a future edition follows a released book`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val bookDate = daysFromToday(-30)
                val editionDate = daysFromToday(30)
                stateFlow.value = buildBookDetailUiState().copy(
                    book = stubBook(bookDate),
                    previewEdition = editionReleasing(editionDate),
                )

                // ----- Act -----
                val job = launchCollector()

                // ----- Assert -----
                stateFlow.value.unreleasedBadge shouldBe editionDate.toEditionUnreleasedBadgeUiModel()
                stateFlow.value.releaseCaption shouldBe "First published ${bookDate.formatLongDate()}."
                job.cancel()
            }

        @Test
        fun `shows the edition badge with a First edition out caption when the edition follows a future book`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val bookDate = daysFromToday(30)
                val editionDate = daysFromToday(60)
                stateFlow.value = buildBookDetailUiState().copy(
                    book = stubBook(bookDate),
                    previewEdition = editionReleasing(editionDate),
                )

                // ----- Act -----
                val job = launchCollector()

                // ----- Assert -----
                stateFlow.value.unreleasedBadge shouldBe editionDate.toEditionUnreleasedBadgeUiModel()
                stateFlow.value.releaseCaption shouldBe "First edition out ${bookDate.formatLongDate()}."
                job.cancel()
            }

        @Test
        fun `shows the book badge and no caption when the edition date equals the future book date`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val date = daysFromToday(30)
                stateFlow.value = buildBookDetailUiState().copy(
                    book = stubBook(date),
                    previewEdition = editionReleasing(date),
                )

                // ----- Act -----
                val job = launchCollector()

                // ----- Assert -----
                stateFlow.value.unreleasedBadge shouldBe date.toProminentUnreleasedBadgeUiModel()
                stateFlow.value.releaseCaption shouldBe null
                job.cancel()
            }

        @Test
        fun `shows the book badge and no caption when the edition is earlier than the future book date`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val bookDate = daysFromToday(60)
                stateFlow.value = buildBookDetailUiState().copy(
                    book = stubBook(bookDate),
                    previewEdition = editionReleasing(daysFromToday(30)),
                )

                // ----- Act -----
                val job = launchCollector()

                // ----- Assert -----
                stateFlow.value.unreleasedBadge shouldBe bookDate.toProminentUnreleasedBadgeUiModel()
                stateFlow.value.releaseCaption shouldBe null
                job.cancel()
            }

        @Test
        fun `clears badge and caption when the edition is in the past and the book is released`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = buildBookDetailUiState().copy(
                    book = stubBook(daysFromToday(-60)),
                    previewEdition = editionReleasing(daysFromToday(-30)),
                )

                // ----- Act -----
                val job = launchCollector()

                // ----- Assert -----
                stateFlow.value.unreleasedBadge shouldBe null
                stateFlow.value.releaseCaption shouldBe null
                job.cancel()
            }

        @Test
        fun `updates badge and caption when the displayed edition changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val bookDate = daysFromToday(-30)
            val editionDate = daysFromToday(30)
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(bookDate))
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(previewEdition = editionReleasing(editionDate))

            // ----- Assert -----
            stateFlow.value.unreleasedBadge shouldBe editionDate.toEditionUnreleasedBadgeUiModel()
            stateFlow.value.releaseCaption shouldBe "First published ${bookDate.formatLongDate()}."
            job.cancel()
        }
    }
}
