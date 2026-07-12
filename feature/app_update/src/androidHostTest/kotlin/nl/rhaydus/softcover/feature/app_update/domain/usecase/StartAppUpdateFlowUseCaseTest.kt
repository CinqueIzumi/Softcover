package nl.rhaydus.softcover.feature.app_update.domain.usecase

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.app_update.domain.launcher.AppUpdateFlowLauncher

class StartAppUpdateFlowUseCaseTest {
    private lateinit var useCase: StartAppUpdateFlowUseCase

    @BeforeEach
    fun setUp() {
        useCase = StartAppUpdateFlowUseCase()
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to launcher launch`() {
            // ----- Arrange -----
            val launcher = mockk<AppUpdateFlowLauncher>(relaxed = true)

            // ----- Act -----
            useCase(launcher)

            // ----- Assert -----
            verify {
                launcher.launch()
            }
        }
    }
}
