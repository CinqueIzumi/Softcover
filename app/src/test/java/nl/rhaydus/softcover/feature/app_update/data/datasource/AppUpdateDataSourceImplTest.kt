package nl.rhaydus.softcover.feature.app_update.data.datasource

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import app.cash.turbine.test
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.app_update.domain.model.AppUpdateState
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AppUpdateDataSourceImplTest {

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var listenerSlot: CapturingSlot<InstallStateUpdatedListener>
    private lateinit var dataSource: AppUpdateDataSourceImpl

    @BeforeEach
    fun setUp() {
        mockkStatic("com.google.android.play.core.ktx.AppUpdateManagerKtxKt")
        appUpdateManager = mockk(relaxed = true)
        listenerSlot = slot()

        every {
            appUpdateManager.registerListener(capture(listenerSlot))
        } returns Unit

        dataSource = AppUpdateDataSourceImpl(appUpdateManager = appUpdateManager)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("com.google.android.play.core.ktx.AppUpdateManagerKtxKt")
    }

    private fun stubInstallState(status: Int): InstallState = mockk {
        every {
            this@mockk.installStatus()
        } returns status
    }

    private fun stubAppUpdateInfo(
        installStatus: Int = InstallStatus.UNKNOWN,
        updateAvailability: Int = UpdateAvailability.UNKNOWN,
        isFlexibleAllowed: Boolean = false,
    ): AppUpdateInfo = mockk {
        every {
            this@mockk.installStatus()
        } returns installStatus

        every {
            this@mockk.updateAvailability()
        } returns updateAvailability

        every {
            this@mockk.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
        } returns isFlexibleAllowed
    }

    @Nested
    inner class InstallListenerTranslations {

        @Test
        fun `listener sets Downloading state when install status is DOWNLOADING`() = runTest {
            // ----- Arrange -----
            val installState = stubInstallState(InstallStatus.DOWNLOADING)

            // ----- Act -----
            listenerSlot.captured.onStateUpdate(installState)

            // ----- Assert -----
            dataSource.updateState.test {
                awaitItem() shouldBe AppUpdateState.Downloading
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `listener sets Downloaded state when install status is DOWNLOADED`() = runTest {
            // ----- Arrange -----
            val installState = stubInstallState(InstallStatus.DOWNLOADED)

            // ----- Act -----
            listenerSlot.captured.onStateUpdate(installState)

            // ----- Assert -----
            dataSource.updateState.test {
                awaitItem() shouldBe AppUpdateState.Downloaded
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `listener sets Failed state when install status is FAILED`() = runTest {
            // ----- Arrange -----
            val installState = stubInstallState(InstallStatus.FAILED)

            // ----- Act -----
            listenerSlot.captured.onStateUpdate(installState)

            // ----- Assert -----
            dataSource.updateState.test {
                awaitItem() shouldBe AppUpdateState.Failed
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `listener sets Idle state when install status is CANCELED`() = runTest {
            // ----- Arrange -----
            val installState = stubInstallState(InstallStatus.CANCELED)

            // ----- Act -----
            listenerSlot.captured.onStateUpdate(installState)

            // ----- Assert -----
            dataSource.updateState.test {
                awaitItem() shouldBe AppUpdateState.Idle
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `listener does not change state for unrecognised install statuses`() = runTest {
            // ----- Arrange -----
            val installState = stubInstallState(InstallStatus.UNKNOWN)

            // ----- Act -----
            listenerSlot.captured.onStateUpdate(installState)

            // ----- Assert -----
            dataSource.updateState.test {
                awaitItem() shouldBe AppUpdateState.Idle
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class CheckForUpdate {

        @Test
        fun `sets Available state when update is available and flexible type is allowed`() = runTest {
            // ----- Arrange -----
            val info = stubAppUpdateInfo(
                installStatus = InstallStatus.UNKNOWN,
                updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
                isFlexibleAllowed = true,
            )

            coEvery {
                appUpdateManager.requestAppUpdateInfo()
            } returns info

            // ----- Act -----
            dataSource.checkForUpdate()

            // ----- Assert -----
            dataSource.updateState.test {
                awaitItem() shouldBe AppUpdateState.Available
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `sets Downloaded state when install status is already DOWNLOADED`() = runTest {
            // ----- Arrange -----
            val info = stubAppUpdateInfo(
                installStatus = InstallStatus.DOWNLOADED,
                updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
                isFlexibleAllowed = true,
            )

            coEvery {
                appUpdateManager.requestAppUpdateInfo()
            } returns info

            // ----- Act -----
            dataSource.checkForUpdate()

            // ----- Assert -----
            dataSource.updateState.test {
                awaitItem() shouldBe AppUpdateState.Downloaded
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `Downloaded takes precedence over Available when both conditions are true`() = runTest {
            // ----- Arrange -----
            val info = stubAppUpdateInfo(
                installStatus = InstallStatus.DOWNLOADED,
                updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
                isFlexibleAllowed = true,
            )

            coEvery {
                appUpdateManager.requestAppUpdateInfo()
            } returns info

            // ----- Act -----
            dataSource.checkForUpdate()

            // ----- Assert -----
            dataSource.updateState.test {
                awaitItem() shouldBe AppUpdateState.Downloaded
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `sets Idle state when update is available but flexible type is not allowed`() = runTest {
            // ----- Arrange -----
            val info = stubAppUpdateInfo(
                installStatus = InstallStatus.UNKNOWN,
                updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
                isFlexibleAllowed = false,
            )

            coEvery {
                appUpdateManager.requestAppUpdateInfo()
            } returns info

            // ----- Act -----
            dataSource.checkForUpdate()

            // ----- Assert -----
            dataSource.updateState.test {
                awaitItem() shouldBe AppUpdateState.Idle
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `sets Idle state when no update is available`() = runTest {
            // ----- Arrange -----
            val info = stubAppUpdateInfo(
                installStatus = InstallStatus.UNKNOWN,
                updateAvailability = UpdateAvailability.UPDATE_NOT_AVAILABLE,
                isFlexibleAllowed = false,
            )

            coEvery {
                appUpdateManager.requestAppUpdateInfo()
            } returns info

            // ----- Act -----
            dataSource.checkForUpdate()

            // ----- Assert -----
            dataSource.updateState.test {
                awaitItem() shouldBe AppUpdateState.Idle
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `sets Idle state and logs when requestAppUpdateInfo throws`() = runTest {
            // ----- Arrange -----
            coEvery {
                appUpdateManager.requestAppUpdateInfo()
            } throws RuntimeException("network error")

            // ----- Act -----
            dataSource.checkForUpdate()

            // ----- Assert -----
            dataSource.updateState.test {
                awaitItem() shouldBe AppUpdateState.Idle
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `caches AppUpdateInfo on success so startUpdateFlow can use it`() = runTest {
            // ----- Arrange -----
            val launcher = mockk<ActivityResultLauncher<IntentSenderRequest>>()
            val info = stubAppUpdateInfo(
                installStatus = InstallStatus.UNKNOWN,
                updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
                isFlexibleAllowed = true,
            )

            coEvery {
                appUpdateManager.requestAppUpdateInfo()
            } returns info

            dataSource.checkForUpdate()

            // ----- Act -----
            dataSource.startUpdateFlow(launcher = launcher)

            // ----- Assert -----
            verify {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    launcher,
                    any<AppUpdateOptions>(),
                )
            }
        }
    }

    @Nested
    inner class StartUpdateFlow {

        @Test
        fun `does nothing when no AppUpdateInfo has been cached`() = runTest {
            // ----- Arrange -----
            val launcher = mockk<ActivityResultLauncher<IntentSenderRequest>>()

            // ----- Act -----
            dataSource.startUpdateFlow(launcher = launcher)

            // ----- Assert -----
            verify(exactly = 0) {
                appUpdateManager.startUpdateFlowForResult(
                    any(),
                    any<ActivityResultLauncher<IntentSenderRequest>>(),
                    any<AppUpdateOptions>(),
                )
            }
        }

        @Test
        fun `calls startUpdateFlowForResult with FLEXIBLE AppUpdateOptions when info is cached`() = runTest {
            // ----- Arrange -----
            val launcher = mockk<ActivityResultLauncher<IntentSenderRequest>>()
            val info = stubAppUpdateInfo(
                installStatus = InstallStatus.UNKNOWN,
                updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
                isFlexibleAllowed = true,
            )
            val capturedOptions = slot<AppUpdateOptions>()

            coEvery {
                appUpdateManager.requestAppUpdateInfo()
            } returns info

            every {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    launcher,
                    capture(capturedOptions),
                )
            } returns true

            dataSource.checkForUpdate()

            // ----- Act -----
            dataSource.startUpdateFlow(launcher = launcher)

            // ----- Assert -----
            capturedOptions.captured.appUpdateType() shouldBe AppUpdateType.FLEXIBLE
        }

        @Test
        fun `sets Failed state when startUpdateFlowForResult throws`() = runTest {
            // ----- Arrange -----
            val launcher = mockk<ActivityResultLauncher<IntentSenderRequest>>()
            val info = stubAppUpdateInfo(
                installStatus = InstallStatus.UNKNOWN,
                updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
                isFlexibleAllowed = true,
            )

            coEvery {
                appUpdateManager.requestAppUpdateInfo()
            } returns info

            every {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    launcher,
                    any<AppUpdateOptions>(),
                )
            } throws RuntimeException("launch failed")

            dataSource.checkForUpdate()

            // ----- Act -----
            dataSource.startUpdateFlow(launcher = launcher)

            // ----- Assert -----
            dataSource.updateState.test {
                awaitItem() shouldBe AppUpdateState.Failed
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class CompleteUpdate {

        @Test
        fun `delegates to appUpdateManager completeUpdate`() {
            // ----- Arrange -----
            // (no additional setup — appUpdateManager is relaxed)

            // ----- Act -----
            dataSource.completeUpdate()

            // ----- Assert -----
            verify {
                appUpdateManager.completeUpdate()
            }
        }
    }

    @Nested
    inner class InitialState {

        @Test
        fun `initial updateState is Idle`() = runTest {
            // ----- Arrange -----
            // (fresh dataSource created in setUp)

            // ----- Act & Assert -----
            dataSource.updateState.test {
                awaitItem() shouldBe AppUpdateState.Idle
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `registers install listener on construction`() {
            // ----- Arrange -----
            // (dataSource was already created in setUp, slot was captured during registerListener)

            // ----- Act -----
            // (construction happened in setUp)

            // ----- Assert -----
            listenerSlot.isCaptured shouldBe true
        }
    }
}
