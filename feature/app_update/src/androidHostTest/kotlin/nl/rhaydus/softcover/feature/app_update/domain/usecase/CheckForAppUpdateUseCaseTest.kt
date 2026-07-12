package nl.rhaydus.softcover.feature.app_update.domain.usecase

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.app_update.domain.repository.AppUpdateRepository

class CheckForAppUpdateUseCaseTest {
    private lateinit var appUpdateRepository: AppUpdateRepository
    private lateinit var useCase: CheckForAppUpdateUseCase

    @BeforeEach
    fun setUp() {
        appUpdateRepository = mockk(relaxed = true)
        useCase = CheckForAppUpdateUseCase(appUpdateRepository = appUpdateRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository checkForUpdate`() = runTest {
            // ----- Arrange -----
            // (no additional setup — repository is relaxed)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                appUpdateRepository.checkForUpdate()
            }
        }
    }
}
