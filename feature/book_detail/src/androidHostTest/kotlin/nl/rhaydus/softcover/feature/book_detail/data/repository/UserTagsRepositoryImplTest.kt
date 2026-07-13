package nl.rhaydus.softcover.feature.book_detail.data.repository

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.data.datasource.UserTagsRemoteDataSource

class UserTagsRepositoryImplTest {
    private lateinit var userTagsRemoteDataSource: UserTagsRemoteDataSource
    private lateinit var repository: UserTagsRepositoryImpl

    @BeforeEach
    fun setUp() {
        userTagsRemoteDataSource = mockk()
        repository = UserTagsRepositoryImpl(
            userTagsRemoteDataSource = userTagsRemoteDataSource,
        )
    }

    private fun stubUserTag(name: String = "Cozy"): UserTag = UserTag(
        name = name,
        category = TagCategory.MOOD,
        count = 2,
        spoiler = false,
    )

    @Nested
    inner class GetUserTags {
        @Test
        fun `delegates to remote data source with the given userId and bookId`() = runTest {
            // ----- Arrange -----
            val userId = 5
            val bookId = 42

            coEvery {
                userTagsRemoteDataSource.getUserTags(
                    userId = userId,
                    bookId = bookId,
                )
            } returns emptyList()

            // ----- Act -----
            repository.getUserTags(
                userId = userId,
                bookId = bookId,
            )

            // ----- Assert -----
            coVerify {
                userTagsRemoteDataSource.getUserTags(
                    userId = userId,
                    bookId = bookId,
                )
            }
        }

        @Test
        fun `returns the list returned by the remote data source`() = runTest {
            // ----- Arrange -----
            val userId = 5
            val bookId = 42
            val expected = listOf(stubUserTag("Cozy"), stubUserTag("Dark"))

            coEvery {
                userTagsRemoteDataSource.getUserTags(
                    userId = userId,
                    bookId = bookId,
                )
            } returns expected

            // ----- Act -----
            val result = repository.getUserTags(
                userId = userId,
                bookId = bookId,
            )

            // ----- Assert -----
            result shouldBe expected
        }
    }

    @Nested
    inner class SaveTags {
        @Test
        fun `delegates to remote data source with the given bookId and tags`() = runTest {
            // ----- Arrange -----
            val bookId = 99
            val tags = listOf(stubUserTag())

            coEvery {
                userTagsRemoteDataSource.saveTags(
                    bookId = bookId,
                    tags = tags,
                )
            } returns emptyList()

            // ----- Act -----
            repository.saveTags(
                bookId = bookId,
                tags = tags,
            )

            // ----- Assert -----
            coVerify {
                userTagsRemoteDataSource.saveTags(
                    bookId = bookId,
                    tags = tags,
                )
            }
        }

        @Test
        fun `returns the list returned by the remote data source`() = runTest {
            // ----- Arrange -----
            val bookId = 99
            val tags = listOf(stubUserTag())
            val expected = listOf(stubUserTag("Cozy"), stubUserTag("Dark"))

            coEvery {
                userTagsRemoteDataSource.saveTags(
                    bookId = bookId,
                    tags = tags,
                )
            } returns expected

            // ----- Act -----
            val result = repository.saveTags(
                bookId = bookId,
                tags = tags,
            )

            // ----- Assert -----
            result shouldBe expected
        }
    }
}
