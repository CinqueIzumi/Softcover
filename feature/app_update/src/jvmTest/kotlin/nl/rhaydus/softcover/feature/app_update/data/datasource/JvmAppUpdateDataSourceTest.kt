package nl.rhaydus.softcover.feature.app_update.data.datasource

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.app.AppVersionInfo
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.app_update.data.install.DesktopInstallerLauncher
import nl.rhaydus.softcover.feature.app_update.data.model.AppRelease
import nl.rhaydus.softcover.feature.app_update.data.release.ReleaseSource
import nl.rhaydus.ui.common.AppDispatchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class JvmAppUpdateDataSourceTest {
    private lateinit var appVersionProvider: AppVersionProvider
    private lateinit var remoteDataSource: ReleaseSource
    private lateinit var installerLauncher: DesktopInstallerLauncher

    @field:TempDir
    lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        appVersionProvider = mockk()
        remoteDataSource = mockk()
        installerLauncher = mockk()

        every {
            appVersionProvider.versionInfo
        } returns AppVersionInfo(
            name = "3.0.1",
            code = 31,
        )
    }

    private fun TestScope.createDataSource(): JvmAppUpdateDataSource {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val appDispatchers = AppDispatchers(
            main = dispatcher,
            io = dispatcher,
            default = dispatcher,
        )
        return JvmAppUpdateDataSource(
            appVersionProvider = appVersionProvider,
            releaseSource = remoteDataSource,
            installerLauncher = installerLauncher,
            appDispatchers = appDispatchers,
            appDataDirectory = tempDir.absolutePath,
        )
    }

    private fun stubRelease(version: String = "9.9.9"): AppRelease = AppRelease(
        version = version,
        installerUrl = "https://example.com/Softcover-v$version-dmg.dmg",
        installerFileName = "Softcover-v$version-dmg.dmg",
    )

    @Nested
    inner class CheckForUpdate {
        @Test
        fun `emits Available when release version is newer than current version`() = runTest {
            // ----- Arrange -----
            val dataSource = createDataSource()

            coEvery {
                remoteDataSource.fetchLatestRelease()
            } returns stubRelease(version = "9.9.9")

            // ----- Act -----
            dataSource.checkForUpdate()
            advanceUntilIdle()

            // ----- Assert -----
            assertEquals(
                AppUpdateState.Available,
                dataSource.updateState.value,
            )
        }

        @Test
        fun `emits Idle when release version matches current version`() = runTest {
            // ----- Arrange -----
            val dataSource = createDataSource()

            coEvery {
                remoteDataSource.fetchLatestRelease()
            } returns stubRelease(version = "3.0.1")

            // ----- Act -----
            dataSource.checkForUpdate()
            advanceUntilIdle()

            // ----- Assert -----
            assertEquals(
                AppUpdateState.Idle,
                dataSource.updateState.value,
            )
        }

        @Test
        fun `emits Idle when release version is older than current version`() = runTest {
            // ----- Arrange -----
            val dataSource = createDataSource()

            coEvery {
                remoteDataSource.fetchLatestRelease()
            } returns stubRelease(version = "1.0.0")

            // ----- Act -----
            dataSource.checkForUpdate()
            advanceUntilIdle()

            // ----- Assert -----
            assertEquals(
                AppUpdateState.Idle,
                dataSource.updateState.value,
            )
        }

        @Test
        fun `emits Idle when fetchLatestRelease returns null`() = runTest {
            // ----- Arrange -----
            val dataSource = createDataSource()

            coEvery {
                remoteDataSource.fetchLatestRelease()
            } returns null

            // ----- Act -----
            dataSource.checkForUpdate()
            advanceUntilIdle()

            // ----- Assert -----
            assertEquals(
                AppUpdateState.Idle,
                dataSource.updateState.value,
            )
        }
    }

    @Nested
    inner class StartDownload {
        @Test
        fun `emits Downloaded after a successful download`() = runTest {
            // ----- Arrange -----
            val dataSource = createDataSource()
            val release = stubRelease()

            coEvery {
                remoteDataSource.fetchLatestRelease()
            } returns release

            coEvery {
                remoteDataSource.downloadInstaller(
                    release = release,
                    destinationDirectory = any(),
                )
            } returns File(
                tempDir,
                release.installerFileName,
            )

            dataSource.checkForUpdate()
            advanceUntilIdle()

            // ----- Act -----
            dataSource.startDownload()
            advanceUntilIdle()

            // ----- Assert -----
            assertEquals(
                AppUpdateState.Downloaded,
                dataSource.updateState.value,
            )
        }

        @Test
        fun `emits Failed when downloadInstaller returns null`() = runTest {
            // ----- Arrange -----
            val dataSource = createDataSource()
            val release = stubRelease()

            coEvery {
                remoteDataSource.fetchLatestRelease()
            } returns release

            coEvery {
                remoteDataSource.downloadInstaller(
                    release = release,
                    destinationDirectory = any(),
                )
            } returns null

            dataSource.checkForUpdate()
            advanceUntilIdle()

            // ----- Act -----
            dataSource.startDownload()
            advanceUntilIdle()

            // ----- Assert -----
            assertEquals(
                AppUpdateState.Failed,
                dataSource.updateState.value,
            )
        }

        @Test
        fun `ignores a second startDownload call while state is Downloading`() = runTest {
            // ----- Arrange -----
            val dataSource = createDataSource()
            val release = stubRelease()

            coEvery {
                remoteDataSource.fetchLatestRelease()
            } returns release

            coEvery {
                remoteDataSource.downloadInstaller(
                    release = release,
                    destinationDirectory = any(),
                )
            } returns File(
                tempDir,
                release.installerFileName,
            )

            dataSource.checkForUpdate()
            advanceUntilIdle()
            // State transitions to Downloading synchronously before the background coroutine runs
            dataSource.startDownload()

            // ----- Act -----
            dataSource.startDownload()

            // ----- Assert -----
            assertEquals(
                AppUpdateState.Downloading,
                dataSource.updateState.value,
            )
            advanceUntilIdle()
            coVerify(exactly = 1) {
                remoteDataSource.downloadInstaller(
                    release = release,
                    destinationDirectory = any(),
                )
            }
        }
    }

    @Nested
    inner class CompleteUpdate {
        @Test
        fun `is a no-op when called before any download`() = runTest {
            // ----- Arrange -----
            val dataSource = createDataSource()

            // ----- Act -----
            dataSource.completeUpdate()

            // ----- Assert -----
            verify(exactly = 0) {
                installerLauncher.launch(installer = any())
            }
            assertEquals(
                AppUpdateState.Idle,
                dataSource.updateState.value,
            )
        }

        @Test
        fun `emits Failed when installer launch returns false`() = runTest {
            // ----- Arrange -----
            val dataSource = createDataSource()
            val release = stubRelease()

            coEvery {
                remoteDataSource.fetchLatestRelease()
            } returns release

            coEvery {
                remoteDataSource.downloadInstaller(
                    release = release,
                    destinationDirectory = any(),
                )
            } returns File(
                tempDir,
                release.installerFileName,
            )

            every {
                installerLauncher.launch(installer = any())
            } returns false

            dataSource.checkForUpdate()
            advanceUntilIdle()
            dataSource.startDownload()
            advanceUntilIdle()

            // ----- Act -----
            dataSource.completeUpdate()

            // ----- Assert -----
            assertEquals(
                AppUpdateState.Failed,
                dataSource.updateState.value,
            )
        }
    }
}
