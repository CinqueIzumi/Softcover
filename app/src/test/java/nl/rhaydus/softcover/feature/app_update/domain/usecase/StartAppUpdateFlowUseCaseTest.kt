package nl.rhaydus.softcover.feature.app_update.domain.usecase

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import io.mockk.mockk
import io.mockk.verify
import nl.rhaydus.softcover.feature.app_update.domain.repository.AppUpdateRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StartAppUpdateFlowUseCaseTest {

    private lateinit var appUpdateRepository: AppUpdateRepository
    private lateinit var useCase: StartAppUpdateFlowUseCase

    @BeforeEach
    fun setUp() {
        appUpdateRepository = mockk(relaxed = true)
        useCase = StartAppUpdateFlowUseCase(appUpdateRepository = appUpdateRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `delegates to repository startUpdateFlow with the provided launcher`() {
            // ----- Arrange -----
            val launcher = mockk<ActivityResultLauncher<IntentSenderRequest>>()

            // ----- Act -----
            useCase(launcher)

            // ----- Assert -----
            verify {
                appUpdateRepository.startUpdateFlow(launcher = launcher)
            }
        }
    }
}
