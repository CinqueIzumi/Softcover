package nl.rhaydus.softcover.core.domain.model

sealed interface AppUpdateState {

    data object Idle : AppUpdateState

    data object Available : AppUpdateState

    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long?,
    ) : AppUpdateState

    data object Downloaded : AppUpdateState

    data object Failed : AppUpdateState
}
