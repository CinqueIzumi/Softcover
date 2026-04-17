package nl.rhaydus.softcover.feature.app_update.data.repository

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.app_update.data.datasource.AppUpdateDataSource
import nl.rhaydus.softcover.feature.app_update.domain.model.AppUpdateState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AppUpdateRepositoryImplTest {

    private lateinit var appUpdateDataSource: AppUpdateDataSource
    private lateinit var repository: AppUpdateRepositoryImpl

    @BeforeEach
    fun setUp() {
        appUpdateDataSource = mockk(relaxed = true)
        repository = AppUpdateRepositoryImpl(appUpdateDataSource = appUpdateDataSource)
    }

    @Nested
    inner class UpdateState {

        @Test
        fun `updateState is wired to data source updateState flow`() = runTest {
            // ----- Arrange -----
            val expectedState = AppUpdateState.Available

            every {
                appUpdateDataSource.updateState
            } returns flowOf(expectedState)

            val freshRepository = AppUpdateRepositoryImpl(appUpdateDataSource = appUpdateDataSource)

            // ----- Act & Assert -----
            freshRepository.updateState.test {
                awaitItem() shouldBe expectedState
                awaitComplete()
            }
        }
    }

    @Nested
    inner class CheckForUpdate {

        @Test
        fun `delegates to data source checkForUpdate`() = runTest {
            // ----- Arrange -----
            // (no additional setup — data source is relaxed)

            // ----- Act -----
            repository.checkForUpdate()

            // ----- Assert -----
            coVerify {
                appUpdateDataSource.checkForUpdate()
            }
        }
    }

    @Nested
    inner class StartUpdateFlow {

        @Test
        fun `delegates to data source startUpdateFlow with the provided launcher`() {
            // ----- Arrange -----
            val launcher = mockk<ActivityResultLauncher<IntentSenderRequest>>()

            // ----- Act -----
            repository.startUpdateFlow(launcher = launcher)

            // ----- Assert -----
            verify {
                appUpdateDataSource.startUpdateFlow(launcher = launcher)
            }
        }
    }

    @Nested
    inner class CompleteUpdate {

        @Test
        fun `delegates to data source completeUpdate`() {
            // ----- Arrange -----
            // (no additional setup — data source is relaxed)

            // ----- Act -----
            repository.completeUpdate()

            // ----- Assert -----
            verify {
                appUpdateDataSource.completeUpdate()
            }
        }
    }
}
