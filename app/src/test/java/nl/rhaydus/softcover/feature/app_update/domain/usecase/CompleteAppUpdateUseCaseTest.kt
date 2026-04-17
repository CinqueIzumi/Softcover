package nl.rhaydus.softcover.feature.app_update.domain.usecase

import io.mockk.mockk
import io.mockk.verify
import nl.rhaydus.softcover.feature.app_update.domain.repository.AppUpdateRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CompleteAppUpdateUseCaseTest {

    private lateinit var appUpdateRepository: AppUpdateRepository
    private lateinit var useCase: CompleteAppUpdateUseCase

    @BeforeEach
    fun setUp() {
        appUpdateRepository = mockk(relaxed = true)
        useCase = CompleteAppUpdateUseCase(appUpdateRepository = appUpdateRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `delegates to repository completeUpdate`() {
            // ----- Arrange -----
            // (no additional setup — repository is relaxed)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            verify {
                appUpdateRepository.completeUpdate()
            }
        }
    }
}
