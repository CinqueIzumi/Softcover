package nl.rhaydus.softcover.core.profile.data.repository

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileLocalDataSource
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileRemoteDataSource
import nl.rhaydus.softcover.core.profile.domain.ProfileRefreshGate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProfileRepositoryImplTest {
    private lateinit var profileLocalDataSource: ProfileLocalDataSource
    private lateinit var activityRefreshGate: ProfileRefreshGate
    private lateinit var statsRefreshGate: ProfileRefreshGate
    private lateinit var repository: ProfileRepositoryImpl

    @BeforeEach
    fun setUp() {
        profileLocalDataSource = mockk(relaxed = true)
        activityRefreshGate = mockk(relaxed = true)
        statsRefreshGate = mockk(relaxed = true)
        repository = ProfileRepositoryImpl(
            profileRemoteDataSource = mockk<ProfileRemoteDataSource>(),
            profileLocalDataSource = profileLocalDataSource,
            activityRefreshGate = activityRefreshGate,
            statsRefreshGate = statsRefreshGate,
        )
    }

    @Nested
    inner class ClearProfileCache {
        @Test
        fun `clears the cache and resets both refresh gates so the next session can refetch`() = runTest {
            // ----- Arrange -----
            // (local data source and refresh gates are relaxed)

            // ----- Act -----
            repository.clearProfileCache()

            // ----- Assert -----
            coVerify(exactly = 1) { profileLocalDataSource.clear() }
            coVerify(exactly = 1) { activityRefreshGate.reset() }
            coVerify(exactly = 1) { statsRefreshGate.reset() }
        }
    }
}
