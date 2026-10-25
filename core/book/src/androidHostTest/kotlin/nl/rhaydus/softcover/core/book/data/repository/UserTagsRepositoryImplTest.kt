package nl.rhaydus.softcover.core.book.data.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.core.book.data.datasource.FakeUserTagsLocalDataSource
import nl.rhaydus.softcover.core.book.data.datasource.UserTagsRemoteDataSource
import nl.rhaydus.softcover.core.book.data.sync.UserBookCacheGate
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.coroutines.cancellation.CancellationException

class UserTagsRepositoryImplTest {
    private val bookId = 7

    private lateinit var remote: UserTagsRemoteDataSource
    private lateinit var local: FakeUserTagsLocalDataSource
    private lateinit var gate: UserBookCacheGate
    private lateinit var repository: UserTagsRepositoryImpl

    private val previous = listOf(
        UserTag(
            name = "Cozy",
            category = TagCategory.MOOD,
        ),
    )
    private val requested = listOf(
        UserTag(
            name = "Fantasy",
            category = TagCategory.GENRE,
        ),
        UserTag(
            name = "Gore",
            category = TagCategory.CONTENT_WARNING,
            spoiler = true,
        ),
    )

    @BeforeEach
    fun setUp() {
        remote = mockk()
        local = FakeUserTagsLocalDataSource()
        gate = UserBookCacheGate(
            booksLocalDataSource = mockk<BooksLocalDataSource>(relaxed = true),
            userTagsLocalDataSource = local,
        )
        repository = UserTagsRepositoryImpl(
            userTagsRemoteDataSource = remote,
            userTagsLocalDataSource = local,
            userBookCacheGate = gate,
        )
    }

    @Nested
    inner class Save {
        @Test
        fun `a successful save stores the set the server returns and returns it`() = runTest {
            // ----- Arrange -----
            val serverSet = listOf(
                UserTag(
                    name = "Fantasy",
                    category = TagCategory.GENRE,
                    count = 42,
                ),
            )
            local.replaceForBook(
                bookId = bookId,
                tags = previous,
            )
            coEvery {
                remote.saveTags(
                    bookId = bookId,
                    tags = requested,
                )
            } returns serverSet

            // ----- Act -----
            val result = repository.save(
                bookId = bookId,
                tags = requested,
            )

            // ----- Assert -----
            result shouldBe serverSet
            local.getForBook(bookId = bookId) shouldBe listOf(serverSet.single().copy(count = 0))
        }

        @Test
        fun `the requested set is visible locally while the server call is in flight`() = runTest {
            // ----- Arrange -----
            var seenDuringCall: List<UserTag> = emptyList()
            coEvery {
                remote.saveTags(
                    bookId = bookId,
                    tags = requested,
                )
            } coAnswers {
                seenDuringCall = local.getForBook(bookId = bookId)
                requested
            }

            // ----- Act -----
            repository.save(
                bookId = bookId,
                tags = requested,
            )

            // ----- Assert -----
            seenDuringCall shouldBe requested
        }

        @Test
        fun `a failed save restores the rows held before it`() = runTest {
            // ----- Arrange -----
            local.replaceForBook(
                bookId = bookId,
                tags = previous,
            )
            coEvery {
                remote.saveTags(
                    bookId = bookId,
                    tags = requested,
                )
            } throws IllegalStateException("offline")

            // ----- Act -----
            val failure = shouldThrow<IllegalStateException> {
                repository.save(
                    bookId = bookId,
                    tags = requested,
                )
            }

            // ----- Assert -----
            failure.message shouldBe "offline"
            local.getForBook(bookId = bookId) shouldBe previous
        }

        @Test
        fun `a failed save keeps a newer write made while it was in flight`() = runTest {
            // ----- Arrange -----
            val newer = listOf(
                UserTag(
                    name = "Slow burn",
                    category = TagCategory.MOOD,
                ),
            )
            local.replaceForBook(
                bookId = bookId,
                tags = previous,
            )
            coEvery {
                remote.saveTags(
                    bookId = bookId,
                    tags = requested,
                )
            } coAnswers {
                gate.writeUserTags(
                    bookId = bookId,
                    rows = newer,
                )
                error("offline")
            }

            // ----- Act -----
            val outcome = runCatching {
                repository.save(
                    bookId = bookId,
                    tags = requested,
                )
            }

            // ----- Assert -----
            outcome.isFailure shouldBe true
            local.getForBook(bookId = bookId) shouldBe newer
        }

        @Test
        fun `a cancelled save propagates and leaves the optimistic rows in place`() = runTest {
            // ----- Arrange -----
            local.replaceForBook(
                bookId = bookId,
                tags = previous,
            )
            coEvery {
                remote.saveTags(
                    bookId = bookId,
                    tags = requested,
                )
            } throws CancellationException("cancelled")

            // ----- Act -----
            val cancellation = shouldThrow<CancellationException> {
                repository.save(
                    bookId = bookId,
                    tags = requested,
                )
            }

            // ----- Assert -----
            cancellation.message shouldBe "cancelled"
            local.getForBook(bookId = bookId) shouldBe requested
        }

        @Test
        fun `a failed local write after the server succeeds still returns the server set`() = runTest {
            // ----- Arrange -----
            val serverSet = listOf(
                UserTag(
                    name = "Fantasy",
                    category = TagCategory.GENRE,
                    count = 42,
                ),
            )
            coEvery {
                remote.saveTags(
                    bookId = bookId,
                    tags = requested,
                )
            } coAnswers {
                local.replaceForBookFailure = IllegalStateException("book row gone")
                serverSet
            }

            // ----- Act -----
            val result = repository.save(
                bookId = bookId,
                tags = requested,
            )

            // ----- Assert -----
            result shouldBe serverSet
        }

        @Test
        fun `a cancelled local write after the server succeeds propagates`() = runTest {
            // ----- Arrange -----
            coEvery {
                remote.saveTags(
                    bookId = bookId,
                    tags = requested,
                )
            } coAnswers {
                local.replaceForBookFailure = CancellationException("cancelled")
                requested
            }

            // ----- Act -----
            val cancellation = shouldThrow<CancellationException> {
                repository.save(
                    bookId = bookId,
                    tags = requested,
                )
            }

            // ----- Assert -----
            cancellation.message shouldBe "cancelled"
        }
    }
}
