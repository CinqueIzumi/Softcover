package nl.rhaydus.softcover.feature.book_detail.data.datasource

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.database.dao.UserTagVocabularyDao
import nl.rhaydus.softcover.core.database.model.UserTagVocabularyEntity

class UserTagVocabularyLocalDataSourceImplTest {
    private lateinit var dao: UserTagVocabularyDao
    private lateinit var dataSource: UserTagVocabularyLocalDataSourceImpl

    @BeforeEach
    fun setUp() {
        dao = mockk(relaxed = true)
        dataSource = UserTagVocabularyLocalDataSourceImpl(dao = dao)
    }

    private fun stubEntity(name: String = "Cozy"): UserTagVocabularyEntity = UserTagVocabularyEntity(
        userId = 7,
        category = "MOOD",
        name = name,
        usageCount = 2,
    )

    @Nested
    inner class Observe {
        @Test
        fun `is wired to dao observe with the given userId`() = runTest {
            // ----- Arrange -----
            val userId = 7
            val expectedEntities = listOf(stubEntity())

            every {
                dao.observe(userId = userId)
            } returns flowOf(expectedEntities)

            // ----- Act & Assert -----
            dataSource.observe(userId = userId).test {
                awaitItem() shouldBe expectedEntities
                awaitComplete()
            }
        }
    }

    @Nested
    inner class UpsertAll {
        @Test
        fun `delegates to dao with the given entities`() = runTest {
            // ----- Arrange -----
            val entities = listOf(stubEntity("Cozy"), stubEntity("Dark"))

            // ----- Act -----
            dataSource.upsertAll(entities = entities)

            // ----- Assert -----
            coVerify {
                dao.upsertAll(entities = entities)
            }
        }

        @Test
        fun `delegates to dao with an empty list`() = runTest {
            // ----- Act -----
            dataSource.upsertAll(entities = emptyList())

            // ----- Assert -----
            coVerify {
                dao.upsertAll(entities = emptyList())
            }
        }
    }

    @Nested
    inner class InsertIfAbsent {
        @Test
        fun `delegates to dao with the given entities`() = runTest {
            // ----- Arrange -----
            val entities = listOf(stubEntity("Cozy"), stubEntity("Dark"))

            // ----- Act -----
            dataSource.insertIfAbsent(entities = entities)

            // ----- Assert -----
            coVerify {
                dao.insertIfAbsent(entities = entities)
            }
        }

        @Test
        fun `delegates to dao with an empty list`() = runTest {
            // ----- Act -----
            dataSource.insertIfAbsent(entities = emptyList())

            // ----- Assert -----
            coVerify {
                dao.insertIfAbsent(entities = emptyList())
            }
        }
    }

    @Nested
    inner class ReplaceAll {
        @Test
        fun `delegates to dao replaceForUser with the given userId and entities`() = runTest {
            // ----- Arrange -----
            val userId = 7
            val entities = listOf(stubEntity("Cozy"), stubEntity("Dark"))

            // ----- Act -----
            dataSource.replaceAll(
                userId = userId,
                entities = entities,
            )

            // ----- Assert -----
            coVerify {
                dao.replaceForUser(
                    userId = userId,
                    entities = entities,
                )
            }
        }

        @Test
        fun `delegates to dao replaceForUser even when the replacement list is empty`() = runTest {
            // ----- Arrange -----
            val userId = 7

            // ----- Act -----
            dataSource.replaceAll(
                userId = userId,
                entities = emptyList(),
            )

            // ----- Assert -----
            coVerify {
                dao.replaceForUser(
                    userId = userId,
                    entities = emptyList(),
                )
            }
        }
    }
}
