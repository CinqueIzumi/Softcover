package nl.rhaydus.softcover.core.profile.data.datasource

import androidx.datastore.core.DataStore
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.profile.data.datastore.ProfileCacheDataStore
import nl.rhaydus.softcover.core.profile.data.model.ProfileCacheEntity
import nl.rhaydus.softcover.core.profile.data.model.UserProfileDataEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProfileLocalDataSourceImplTest {
    private lateinit var store: DataStore<ProfileCacheEntity>
    private lateinit var dataSource: ProfileLocalDataSourceImpl

    private val baseProfile = UserProfileDataEntity(
        profileImageUrl = "",
        name = "",
        username = "",
        bio = "",
        readingStreak = 0,
        recentReadingDays = emptyList(),
    )

    @BeforeEach
    fun setUp() {
        store = mockk()
        dataSource = ProfileLocalDataSourceImpl(
            profileCacheDataStore = ProfileCacheDataStore(store = store),
        )
    }

    private fun stubStore(seed: ProfileCacheEntity): () -> ProfileCacheEntity {
        var result = seed

        coEvery {
            store.updateData(any())
        } coAnswers {
            val transform = firstArg<suspend (ProfileCacheEntity) -> ProfileCacheEntity>()
            transform(seed).also { result = it }
        }

        return { result }
    }

    @Nested
    inner class MarkActiveReadingDate {
        private val date = LocalDate(
            2026,
            6,
            19,
        )

        @Test
        fun `adds new date and keeps list sorted ascending`() = runTest {
            // ----- Arrange -----
            val seed = ProfileCacheEntity(
                profile = baseProfile.copy(recentReadingDays = listOf("2026-06-17")),
            )
            val result = stubStore(seed)

            // ----- Act -----
            dataSource.markActiveReadingDate(date)

            // ----- Assert -----
            result().profile.shouldNotBeNull().recentReadingDays shouldBe listOf("2026-06-17", "2026-06-19")
        }

        @Test
        fun `is a no-op when date is already present`() = runTest {
            // ----- Arrange -----
            val seed = ProfileCacheEntity(
                profile = baseProfile.copy(recentReadingDays = listOf("2026-06-19")),
            )
            val result = stubStore(seed)

            // ----- Act -----
            dataSource.markActiveReadingDate(date)

            // ----- Assert -----
            result().profile.shouldNotBeNull().recentReadingDays shouldBe listOf("2026-06-19")
        }

        @Test
        fun `is a no-op when cached profile is null`() = runTest {
            // ----- Arrange -----
            val seed = ProfileCacheEntity(profile = null)
            val result = stubStore(seed)

            // ----- Act -----
            dataSource.markActiveReadingDate(date)

            // ----- Assert -----
            result() shouldBe seed
        }
    }
}
