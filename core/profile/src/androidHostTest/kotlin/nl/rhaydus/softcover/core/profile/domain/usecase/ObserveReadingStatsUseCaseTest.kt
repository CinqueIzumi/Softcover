package nl.rhaydus.softcover.core.profile.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveUserTagsByBookUseCase
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.profile.domain.mapper.readBook
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ObserveReadingStatsUseCaseTest {
    private lateinit var getReadUserBooksUseCase: GetReadUserBooksUseCase
    private lateinit var observeUserTagsByBookUseCase: ObserveUserTagsByBookUseCase
    private lateinit var dispatcher: TestDispatcher
    private lateinit var useCase: ObserveReadingStatsUseCase

    @BeforeEach
    fun setUp() {
        getReadUserBooksUseCase = mockk()
        observeUserTagsByBookUseCase = mockk()
        every {
            observeUserTagsByBookUseCase()
        } returns flowOf(emptyMap())
        dispatcher = StandardTestDispatcher()
        useCase = ObserveReadingStatsUseCase(
            getReadUserBooksUseCase = getReadUserBooksUseCase,
            observeUserTagsByBookUseCase = observeUserTagsByBookUseCase,
            appDispatchers = AppDispatchers(
                main = dispatcher,
                io = dispatcher,
                default = dispatcher,
            ),
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `aggregates each emission of the read books and skips repeats`() = runTest(dispatcher) {
            // ----- Arrange -----
            val one = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-01",
                    pages = 100,
                ),
            )
            val two = one + readBook(
                id = 2,
                finishedAt = "2026-02-01",
                pages = 50,
            )
            every {
                getReadUserBooksUseCase()
            } returns flowOf(
                one,
                one,
                two,
            )

            // ----- Act & Assert -----
            useCase(StatsRange.Unbounded).test {
                awaitItem().totalPages shouldBe 100
                awaitItem().totalPages shouldBe 150
                awaitComplete()
            }
        }

        @Test
        fun `re-emits with the tag counted when a user tag emission arrives`() = runTest(dispatcher) {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-01",
                ),
            )
            val tagsFlow = MutableSharedFlow<Map<Int, List<UserTag>>>(replay = 1)
            tagsFlow.tryEmit(emptyMap())
            every {
                getReadUserBooksUseCase()
            } returns flowOf(books)
            every {
                observeUserTagsByBookUseCase()
            } returns tagsFlow

            // ----- Act & Assert -----
            useCase(StatsRange.Unbounded).test {
                awaitItem().genres.slices shouldBe emptyList()

                tagsFlow.emit(
                    mapOf(
                        1 to listOf(
                            UserTag(
                                name = "Horror",
                                category = TagCategory.GENRE,
                            ),
                        ),
                    ),
                )

                awaitItem().genres.slices.map { it.name } shouldBe listOf("Horror")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
