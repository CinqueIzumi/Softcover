package nl.rhaydus.softcover.feature.book_detail.data.repository

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.model.UserTagVocabularyEntity
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.data.datasource.UserTagVocabularyLocalDataSource
import nl.rhaydus.softcover.feature.book_detail.data.datasource.UserTagVocabularyRemoteDataSource
import nl.rhaydus.softcover.feature.book_detail.data.mapper.toUserTag
import nl.rhaydus.softcover.feature.book_detail.data.mapper.toVocabularyEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserTagVocabularyRepositoryImplTest {
    private lateinit var remoteDataSource: UserTagVocabularyRemoteDataSource
    private lateinit var localDataSource: UserTagVocabularyLocalDataSource
    private lateinit var repository: UserTagVocabularyRepositoryImpl

    @BeforeEach
    fun setUp() {
        remoteDataSource = mockk()
        localDataSource = mockk(relaxed = true)
        repository = UserTagVocabularyRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
        )
    }

    private fun stubUserTag(name: String = "Cozy"): UserTag = UserTag(
        name = name,
        category = TagCategory.MOOD,
        count = 3,
        spoiler = false,
    )

    private fun stubEntity(name: String = "Cozy"): UserTagVocabularyEntity = UserTagVocabularyEntity(
        userId = 7,
        category = "MOOD",
        name = name,
        usageCount = 3,
    )

    @Nested
    inner class Observe {
        @Test
        fun `maps the entities emitted by the local data source to UserTags`() = runTest {
            // ----- Arrange -----
            val userId = 7
            val entities = listOf(stubEntity("Cozy"), stubEntity("Dark"))

            every {
                localDataSource.observe(userId = userId)
            } returns flowOf(entities)

            // ----- Act & Assert -----
            repository.observe(userId = userId).test {
                awaitItem() shouldBe entities.map { it.toUserTag() }
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SyncFromRemote {
        @Test
        fun `fetches the user vocabulary from remote and replaces the local rows with the mapped entities`() = runTest {
            // ----- Arrange -----
            val userId = 7
            val tags = listOf(stubUserTag("Cozy"), stubUserTag("Dark"))

            coEvery {
                remoteDataSource.fetchUserVocabulary(userId = userId)
            } returns tags

            // ----- Act -----
            repository.syncFromRemote(userId = userId)

            // ----- Assert -----
            coVerify {
                localDataSource.replaceAll(
                    userId = userId,
                    entities = tags.map { it.toVocabularyEntity(userId = userId) },
                )
            }
        }
    }

    @Nested
    inner class Record {
        @Test
        fun `forces usageCount to 1 even when the incoming tag count is the tag's global popularity`() = runTest {
            // ----- Arrange -----
            val userId = 7
            val tags = listOf(stubUserTag("Cozy").copy(count = 50_000))

            // ----- Act -----
            repository.record(
                userId = userId,
                tags = tags,
            )

            // ----- Assert -----
            coVerify {
                localDataSource.insertIfAbsent(
                    entities = listOf(
                        UserTagVocabularyEntity(
                            userId = userId,
                            category = "MOOD",
                            name = "Cozy",
                            usageCount = 1,
                        ),
                    ),
                )
            }
        }
    }
}
