package nl.rhaydus.softcover.feature.app_update.data.model

/**
 * A provider-agnostic view of an available desktop release: the version to compare against the
 * running build, and the installer to download for the host OS. Returned by
 * [AppUpdateRemoteDataSource][nl.rhaydus.softcover.feature.app_update.data.datasource.AppUpdateRemoteDataSource]
 * so the state machine never sees where the release came from.
 */
internal data class AppRelease(
    val version: String,
    val installerUrl: String,
    val installerFileName: String,
)
