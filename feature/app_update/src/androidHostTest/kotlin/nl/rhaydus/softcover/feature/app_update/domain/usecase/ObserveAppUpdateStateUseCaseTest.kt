package nl.rhaydus.softcover.feature.app_update.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.app_update.domain.repository.AppUpdateRepository

class ObserveAppUpdateStateUseCaseTest {
    private lateinit var appUpdateRepository: AppUpdateRepository
    private lateinit var useCase: ObserveAppUpdateStateUseCase

    @BeforeEach
    fun setUp() {
        appUpdateRepository = mockk()
        useCase = ObserveAppUpdateStateUseCase(appUpdateRepository = appUpdateRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns the updateState flow from the repository`() = runTest {
            // ----- Arrange -----
            val expectedState = AppUpdateState.Available

            every {
                appUpdateRepository.updateState
            } returns flowOf(expectedState)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe expectedState
                awaitComplete()
            }
        }

        @Test
        fun `returns Idle state when repository emits Idle`() = runTest {
            // ----- Arrange -----
            every {
                appUpdateRepository.updateState
            } returns flowOf(AppUpdateState.Idle)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe AppUpdateState.Idle
                awaitComplete()
            }
        }

        @Test
        fun `returns Downloading state when repository emits Downloading`() = runTest {
            // ----- Arrange -----
            every {
                appUpdateRepository.updateState
            } returns flowOf(AppUpdateState.Downloading)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe AppUpdateState.Downloading
                awaitComplete()
            }
        }

        @Test
        fun `returns Downloaded state when repository emits Downloaded`() = runTest {
            // ----- Arrange -----
            every {
                appUpdateRepository.updateState
            } returns flowOf(AppUpdateState.Downloaded)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe AppUpdateState.Downloaded
                awaitComplete()
            }
        }

        @Test
        fun `returns Failed state when repository emits Failed`() = runTest {
            // ----- Arrange -----
            every {
                appUpdateRepository.updateState
            } returns flowOf(AppUpdateState.Failed)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe AppUpdateState.Failed
                awaitComplete()
            }
        }

        @Test
        fun `returns multiple sequential states from the repository flow`() = runTest {
            // ----- Arrange -----
            every {
                appUpdateRepository.updateState
            } returns flowOf(
                AppUpdateState.Available,
                AppUpdateState.Downloading,
                AppUpdateState.Downloaded,
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe AppUpdateState.Available
                awaitItem() shouldBe AppUpdateState.Downloading
                awaitItem() shouldBe AppUpdateState.Downloaded
                awaitComplete()
            }
        }
    }
}
