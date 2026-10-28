package nl.rhaydus.softcover.core.profile.data.datasource

import androidx.datastore.core.DataStore
import io.kotest.matchers.nulls.shouldBeNull
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
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
    inner class Clear {
        @Test
        fun `removes the cached profile`() = runTest {
            // ----- Arrange -----
            val seed = ProfileCacheEntity(profile = baseProfile)
            val result = stubStore(seed)

            // ----- Act -----
            dataSource.clear()

            // ----- Assert -----
            result().profile.shouldBeNull()
        }
    }
}
