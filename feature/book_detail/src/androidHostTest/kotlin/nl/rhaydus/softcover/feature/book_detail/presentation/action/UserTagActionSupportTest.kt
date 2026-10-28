package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.SaveUserTagsUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.RecordAppliedTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.UserTagsLoad
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserTagActionSupportTest {
    private lateinit var saveUserTagsUseCase: SaveUserTagsUseCase
    private lateinit var recordAppliedTagsUseCase: RecordAppliedTagsUseCase
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        saveUserTagsUseCase = mockk()
        recordAppliedTagsUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )

        coEvery {
            recordAppliedTagsUseCase(any())
        } returns Result.success(Unit)
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.saveUserTagsUseCase
            } returns saveUserTagsUseCase
            every {
                mock.recordAppliedTagsUseCase
            } returns recordAppliedTagsUseCase
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

    private fun stubBook(id: Int = 1): Book = mockk {
        every {
            this@mockk.id
        } returns id
    }

    private fun stubTag(name: String): UserTag = UserTag(
        name = name,
        category = TagCategory.GENRE,
    )

    private fun loadedState(): BookDetailUiState = stateFlow.value.copy(
        book = stubBook(),
        userTagsLoad = UserTagsLoad.Loaded,
    )

    @Nested
    inner class CommitUserTags {
        @Test
        fun `records the saved tags as applied when the save succeeds`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val saved = listOf(stubTag("cozy"))
            stateFlow.value = loadedState()

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = any(),
                )
            } returns Result.success(saved)

            // ----- Act -----
            scope.commitUserTags(
                newSet = listOf(stubTag("cozy")),
                dependencies = dependencies,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                recordAppliedTagsUseCase(saved)
            }
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `sends a mutation failure and records nothing when the save fails`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = loadedState()

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = any(),
                )
            } returns Result.failure(RuntimeException("network error"))

            // ----- Act -----
            scope.commitUserTags(
                newSet = listOf(stubTag("cozy")),
                dependencies = dependencies,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.MutationFailedEvent>()
            coVerify(exactly = 0) {
                recordAppliedTagsUseCase(any())
            }
        }

        @Test
        fun `leaves the displayed tags to the store when the save fails`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val shown = stubTag("sci-fi")
            stateFlow.value = loadedState().copy(userTags = persistentListOf(shown))

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = any(),
                )
            } returns Result.failure(RuntimeException("network error"))

            // ----- Act -----
            scope.commitUserTags(
                newSet = listOf(stubTag("thriller")),
                dependencies = dependencies,
            )

            // ----- Assert -----
            stateFlow.value.userTags shouldBe listOf(shown)
        }

        @Test
        fun `cancels the in-flight save when a second save starts`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val firstGate = CompletableDeferred<Unit>()
            val first = listOf(stubTag("first"))
            val second = listOf(stubTag("second"))
            stateFlow.value = loadedState()

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = first,
                )
            } coAnswers {
                firstGate.await()
                Result.success(first)
            }
            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = second,
                )
            } returns Result.success(second)

            scope.commitUserTags(
                newSet = first,
                dependencies = dependencies,
            )
            val firstJob = localVariablesFlow.value.tagSaveJob.shouldNotBeNull()

            // ----- Act -----
            scope.commitUserTags(
                newSet = second,
                dependencies = dependencies,
            )
            firstGate.complete(Unit)

            // ----- Assert -----
            firstJob.isCancelled shouldBe true
            coVerify(exactly = 0) {
                recordAppliedTagsUseCase(first)
            }
            coVerify(exactly = 1) {
                recordAppliedTagsUseCase(second)
            }
        }

        @Test
        fun `saves nothing when there is no book`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = loadedState().copy(book = null)

            // ----- Act -----
            scope.commitUserTags(
                newSet = listOf(stubTag("cozy")),
                dependencies = dependencies,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                saveUserTagsUseCase(
                    bookId = any(),
                    tags = any(),
                )
            }
        }
    }
}
