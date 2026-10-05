package nl.rhaydus.softcover.feature.app_update.data.datasource

import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
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
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AppUpdateDataSourceImplTest {
    companion object {
        private const val KTX_CLASS = "com.google.android.play.core.ktx.AppUpdateManagerKtxKt"

        @JvmStatic
        @BeforeAll
        fun mockKtx() {
            mockkStatic(KTX_CLASS)
        }

        @JvmStatic
        @AfterAll
        fun unmockKtx() {
            unmockkStatic(KTX_CLASS)
        }
    }

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var listenerSlot: CapturingSlot<InstallStateUpdatedListener>
    private lateinit var dataSource: AppUpdateDataSourceImpl

    @BeforeEach
    fun setUp() {
        appUpdateManager = mockk(relaxed = true)
        listenerSlot = slot()

        every {
            appUpdateManager.registerListener(capture(listenerSlot))
        } returns Unit

        dataSource = AppUpdateDataSourceImpl(appUpdateManager = appUpdateManager)
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

    private fun stubUpdateInfo(info: AppUpdateInfo) {
        coEvery {
            appUpdateManager.requestAppUpdateInfo()
        } returns info
    }

    @Nested
    inner class InstallListener {
        @Test
        fun `maps DOWNLOADING to Downloading`() {
            // ----- Arrange -----
            val installState = stubInstallState(InstallStatus.DOWNLOADING)

            // ----- Act -----
            listenerSlot.captured.onStateUpdate(installState)

            // ----- Assert -----
            dataSource.updateState.value shouldBe AppUpdateState.Downloading
        }

        @Test
        fun `maps DOWNLOADED to Downloaded`() {
            // ----- Arrange -----
            val installState = stubInstallState(InstallStatus.DOWNLOADED)

            // ----- Act -----
            listenerSlot.captured.onStateUpdate(installState)

            // ----- Assert -----
            dataSource.updateState.value shouldBe AppUpdateState.Downloaded
        }

        @Test
        fun `maps FAILED to Failed`() {
            // ----- Arrange -----
            val installState = stubInstallState(InstallStatus.FAILED)

            // ----- Act -----
            listenerSlot.captured.onStateUpdate(installState)

            // ----- Assert -----
            dataSource.updateState.value shouldBe AppUpdateState.Failed
        }

        @Test
        fun `maps CANCELED back to Idle`() {
            // ----- Arrange -----
            dataSource.forceState(AppUpdateState.Downloading)

            val installState = stubInstallState(InstallStatus.CANCELED)

            // ----- Act -----
            listenerSlot.captured.onStateUpdate(installState)

            // ----- Assert -----
            dataSource.updateState.value shouldBe AppUpdateState.Idle
        }

        @Test
        fun `maps INSTALLED back to Idle`() {
            // ----- Arrange -----
            dataSource.forceState(AppUpdateState.Downloaded)

            val installState = stubInstallState(InstallStatus.INSTALLED)

            // ----- Act -----
            listenerSlot.captured.onStateUpdate(installState)

            // ----- Assert -----
            dataSource.updateState.value shouldBe AppUpdateState.Idle
        }

        @Test
        fun `keeps the current state for unrecognised install statuses`() {
            // ----- Arrange -----
            dataSource.forceState(AppUpdateState.Downloading)

            val installState = stubInstallState(InstallStatus.UNKNOWN)

            // ----- Act -----
            listenerSlot.captured.onStateUpdate(installState)

            // ----- Assert -----
            dataSource.updateState.value shouldBe AppUpdateState.Downloading
        }
    }

    @Nested
    inner class CheckForUpdate {
        @Test
        fun `sets Available when update is available and flexible type is allowed`() = runTest {
            // ----- Arrange -----
            stubUpdateInfo(
                stubAppUpdateInfo(
                    updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
                    isFlexibleAllowed = true,
                ),
            )

            // ----- Act -----
            dataSource.checkForUpdate()

            // ----- Assert -----
            dataSource.updateState.value shouldBe AppUpdateState.Available
        }

        @Test
        fun `Downloaded takes precedence over Available`() = runTest {
            // ----- Arrange -----
            stubUpdateInfo(
                stubAppUpdateInfo(
                    installStatus = InstallStatus.DOWNLOADED,
                    updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
                    isFlexibleAllowed = true,
                ),
            )

            // ----- Act -----
            dataSource.checkForUpdate()

            // ----- Assert -----
            dataSource.updateState.value shouldBe AppUpdateState.Downloaded
        }

        @Test
        fun `sets Idle when update is available but flexible type is not allowed`() = runTest {
            // ----- Arrange -----
            dataSource.forceState(AppUpdateState.Available)

            stubUpdateInfo(
                stubAppUpdateInfo(
                    updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
                    isFlexibleAllowed = false,
                ),
            )

            // ----- Act -----
            dataSource.checkForUpdate()

            // ----- Assert -----
            dataSource.updateState.value shouldBe AppUpdateState.Idle
        }

        @Test
        fun `sets Idle when no update is available`() = runTest {
            // ----- Arrange -----
            dataSource.forceState(AppUpdateState.Available)

            stubUpdateInfo(stubAppUpdateInfo(updateAvailability = UpdateAvailability.UPDATE_NOT_AVAILABLE))

            // ----- Act -----
            dataSource.checkForUpdate()

            // ----- Assert -----
            dataSource.updateState.value shouldBe AppUpdateState.Idle
        }

        @Test
        fun `sets Idle when requesting the update info throws`() = runTest {
            // ----- Arrange -----
            dataSource.forceState(AppUpdateState.Available)

            coEvery {
                appUpdateManager.requestAppUpdateInfo()
            } throws RuntimeException("network error")

            // ----- Act -----
            dataSource.checkForUpdate()

            // ----- Assert -----
            dataSource.updateState.value shouldBe AppUpdateState.Idle
        }
    }
}
