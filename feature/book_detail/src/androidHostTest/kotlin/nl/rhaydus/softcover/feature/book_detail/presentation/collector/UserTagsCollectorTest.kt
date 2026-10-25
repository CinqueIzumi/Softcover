package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveUserTagsForBookUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshUserTagsForBookUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserTag
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

class UserTagsCollectorTest {
    private lateinit var observeUserTagsForBookUseCase: ObserveUserTagsForBookUseCase
    private lateinit var refreshUserTagsForBookUseCase: RefreshUserTagsForBookUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        observeUserTagsForBookUseCase = mockk()
        refreshUserTagsForBookUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeUserTagsForBookUseCase
            } returns observeUserTagsForBookUseCase
            every {
                mock.refreshUserTagsForBookUseCase
            } returns refreshUserTagsForBookUseCase
        }
    }

    private fun stubShelvedBook(id: Int = 1): Book = mockk {
        every {
            this@mockk.id
        } returns id
        every {
            this@mockk.userBook
        } returns mockk<UserBook>()
    }

    private fun stubUnshelvedBook(id: Int = 2): Book = mockk {
        every {
            this@mockk.id
        } returns id
        every {
            this@mockk.userBook
        } returns null
    }

    private fun stubTag(name: String): UserTag = UserTag(
        name = name,
        category = TagCategory.GENRE,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `shows the stored tags before the refresh completes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val tags = listOf(stubTag("fantasy"))
            val refreshGate = CompletableDeferred<Unit>()

            every {
                observeUserTagsForBookUseCase(bookId = 10)
            } returns flowOf(tags)
            coEvery {
                refreshUserTagsForBookUseCase(bookId = 10)
            } coAnswers {
                refreshGate.await()
                Result.success(Unit)
            }

            val job = launch {
                UserTagsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = stubShelvedBook(id = 10))

            // ----- Assert -----
            stateFlow.value.userTags shouldBe tags
            stateFlow.value.userTagsLoad shouldBe UserTagsLoad.Loading
            job.cancel()
        }

        @Test
        fun `marks the tags loaded once the refresh succeeds`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val tags = listOf(stubTag("fantasy"))

            every {
                observeUserTagsForBookUseCase(bookId = 10)
            } returns flowOf(tags)
            coEvery {
                refreshUserTagsForBookUseCase(bookId = 10)
            } returns Result.success(Unit)

            val job = launch {
                UserTagsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = stubShelvedBook(id = 10))

            // ----- Assert -----
            stateFlow.value.userTags shouldBe tags
            stateFlow.value.userTagsLoad shouldBe UserTagsLoad.Loaded
            job.cancel()
        }

        @Test
        fun `marks the tags failed but keeps showing the stored rows when the refresh fails`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val tags = listOf(stubTag("fantasy"))

                every {
                    observeUserTagsForBookUseCase(bookId = 3)
                } returns flowOf(tags)
                coEvery {
                    refreshUserTagsForBookUseCase(bookId = 3)
                } returns Result.failure(RuntimeException("network error"))

                val job = launch {
                    UserTagsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(book = stubShelvedBook(id = 3))

                // ----- Assert -----
                stateFlow.value.userTags shouldBe tags
                stateFlow.value.userTagsLoad shouldBe UserTagsLoad.Failed
                job.cancel()
            }

        @Test
        fun `follows later changes to the stored tags`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val storedTags = MutableStateFlow(listOf(stubTag("fantasy")))

            every {
                observeUserTagsForBookUseCase(bookId = 10)
            } returns storedTags
            coEvery {
                refreshUserTagsForBookUseCase(bookId = 10)
            } returns Result.success(Unit)

            val job = launch {
                UserTagsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(book = stubShelvedBook(id = 10))

            // ----- Act -----
            storedTags.value = listOf(stubTag("fantasy"), stubTag("cozy"))

            // ----- Assert -----
            stateFlow.value.userTags.map { it.name } shouldBe listOf("fantasy", "cozy")
            job.cancel()
        }

        @Test
        fun `neither observes nor refreshes when the book is not on a shelf`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                UserTagsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = stubUnshelvedBook(id = 5))

            // ----- Assert -----
            stateFlow.value.userTagsLoad shouldBe UserTagsLoad.Loading
            coVerify(exactly = 0) {
                refreshUserTagsForBookUseCase(bookId = any())
            }
            job.cancel()
        }

        @Test
        fun `refreshes the book only once when unrelated state changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubShelvedBook(id = 10)

            every {
                observeUserTagsForBookUseCase(bookId = 10)
            } returns flowOf(emptyList())
            coEvery {
                refreshUserTagsForBookUseCase(bookId = 10)
            } returns Result.success(Unit)

            val job = launch {
                UserTagsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(tagEditorInput = "cozy")
            stateFlow.value = stateFlow.value.copy(book = stubShelvedBook(id = 10))

            // ----- Assert -----
            coVerify(exactly = 1) {
                refreshUserTagsForBookUseCase(bookId = 10)
            }
            job.cancel()
        }

        @Test
        fun `swaps to the other book's tags and refreshes it when the book changes`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val tagsA = listOf(stubTag("horror"))
                val tagsB = listOf(stubTag("comedy"))

                every {
                    observeUserTagsForBookUseCase(bookId = 1)
                } returns flowOf(tagsA)
                every {
                    observeUserTagsForBookUseCase(bookId = 2)
                } returns flowOf(tagsB)
                coEvery {
                    refreshUserTagsForBookUseCase(bookId = any())
                } returns Result.success(Unit)

                val job = launch {
                    UserTagsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                stateFlow.value = stateFlow.value.copy(book = stubShelvedBook(id = 1))

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(book = stubShelvedBook(id = 2))

                // ----- Assert -----
                stateFlow.value.userTags shouldBe tagsB
                coVerify(exactly = 1) {
                    refreshUserTagsForBookUseCase(bookId = 1)
                }
                coVerify(exactly = 1) {
                    refreshUserTagsForBookUseCase(bookId = 2)
                }
                job.cancel()
            }

        @Test
        fun `resets to loading with no rows while the next book refreshes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val refreshGate = CompletableDeferred<Unit>()

            every {
                observeUserTagsForBookUseCase(bookId = 1)
            } returns flowOf(listOf(stubTag("horror")))
            every {
                observeUserTagsForBookUseCase(bookId = 2)
            } returns flowOf(emptyList())
            coEvery {
                refreshUserTagsForBookUseCase(bookId = 1)
            } returns Result.success(Unit)
            coEvery {
                refreshUserTagsForBookUseCase(bookId = 2)
            } coAnswers {
                refreshGate.await()
                Result.success(Unit)
            }

            val job = launch {
                UserTagsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(book = stubShelvedBook(id = 1))
            stateFlow.value.userTagsLoad shouldBe UserTagsLoad.Loaded

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = stubShelvedBook(id = 2))

            // ----- Assert -----
            stateFlow.value.userTags shouldBe emptyList()
            stateFlow.value.userTagsLoad shouldBe UserTagsLoad.Loading
            job.cancel()
        }

        @Test
        fun `keeps the loaded tags when the book goes away`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val tags = listOf(stubTag("romance"))

            every {
                observeUserTagsForBookUseCase(bookId = 1)
            } returns flowOf(tags)
            coEvery {
                refreshUserTagsForBookUseCase(bookId = 1)
            } returns Result.success(Unit)

            val job = launch {
                UserTagsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(book = stubShelvedBook(id = 1))

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = null)

            // ----- Assert -----
            stateFlow.value.userTags shouldBe tags
            stateFlow.value.userTagsLoad shouldBe UserTagsLoad.Loaded
            job.cancel()
        }
    }
}
