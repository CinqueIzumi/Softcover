package nl.rhaydus.softcover.feature.app_update.data.release

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.app_update.data.install.DesktopInstallerTarget
import nl.rhaydus.softcover.feature.app_update.data.model.AppRelease
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Provider-agnostic access to the desktop app's release feed. Named for its role, not its backing:
 * the state machine depends on this so the release provider can change without touching it. Returns
 * only the neutral [AppRelease] — no provider-specific types cross this boundary.
 */
internal interface ReleaseSource {
    /**
     * The latest release with an installer for the host OS, or `null` when there is none (no
     * release, no matching installer asset, an unrecognised OS, or a failed request).
     */
    suspend fun fetchLatestRelease(): AppRelease?

    /**
     * Downloads [release]'s installer into [destinationDirectory], returning the downloaded file or
     * `null` on failure (any partial file is discarded). [onProgress] receives the bytes written so
     * far and the total size, `null` while the server has not reported one.
     */
    suspend fun downloadInstaller(
        release: AppRelease,
        destinationDirectory: String,
        onProgress: (bytesDownloaded: Long, totalBytes: Long?) -> Unit,
    ): File?
}

/**
 * GitHub Releases-backed [ReleaseSource]. Everything GitHub — the HTTP calls (on the JDK's
 * `java.net.http`, so the feature adds no runtime HTTP dependency), the response DTOs, and the
 * OS→asset-name matching (`Softcover-vX.Y.Z-{dmg,msi,deb}.{ext}`, see `.github/workflows/release.yml`)
 * — is confined here, then mapped to the neutral [AppRelease]. Only unauthenticated public reads are
 * made (one check per launch), so the 60-req/hour limit is never a concern; a `User-Agent` header is
 * mandatory, as GitHub rejects requests without one. Network work runs on the IO dispatcher.
 */
internal class GitHubReleaseSource(private val appDispatchers: AppDispatchers) : ReleaseSource {
    // Release asset downloads 302-redirect to GitHub's CDN, so the client must follow redirects (the
    // JDK default is NEVER, which would surface the 302 as a failed download). NORMAL follows
    // same-scheme redirects — GitHub stays on HTTPS — without allowing an HTTPS→HTTP downgrade.
    //
    // Built lazily so class linking happens on the first update check (inside the runCatching in
    // requestLatestRelease/downloadTo), not when Koin constructs this at graph build time during the
    // first composition. If java.net.http is ever missing from the packaged runtime again, that
    // degrades the updater to a logged no-op instead of taking the whole app down at launch.
    //
    // Deliberately not closed on shutdown: the JDK HttpClient's selector/executor threads are daemon
    // (they never block JVM exit), and `HttpClient.close()` on JDK 21+ *blocks* until in-flight
    // requests finish — so closing it during the desktop shutdown path could stall a window-close
    // mid-download. The guaranteed `exitProcess(0)` reclaims it instead.
    private val httpClient: HttpClient by lazy {
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchLatestRelease(): AppRelease? {
        return withContext(appDispatchers.io) {
            val target = DesktopInstallerTarget.current() ?: return@withContext null

            val release = requestLatestRelease() ?: return@withContext null

            val asset = release.assets.firstOrNull { it.name.endsWith(target.assetSuffix) }
                ?: return@withContext null

            AppRelease(
                version = release.tagName,
                installerUrl = asset.browserDownloadUrl,
                installerFileName = asset.name,
            )
        }
    }

    override suspend fun downloadInstaller(
        release: AppRelease,
        destinationDirectory: String,
        onProgress: (bytesDownloaded: Long, totalBytes: Long?) -> Unit,
    ): File? {
        return withContext(appDispatchers.io) {
            val destination = File(
                destinationDirectory,
                release.installerFileName,
            )

            var downloaded = false

            try {
                downloaded = downloadTo(
                    url = release.installerUrl,
                    destination = destination,
                    onProgress = onProgress,
                )
            } finally {
                if (downloaded.not()) destination.delete()
            }

            if (downloaded) destination else null
        }
    }

    private fun requestLatestRelease(): GitHubReleaseDto? {
        return runCatching {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/$REPOSITORY_PATH/releases/latest"))
                .header(
                    "Accept",
                    "application/vnd.github+json",
                )
                .header(
                    "User-Agent",
                    USER_AGENT,
                )
                .GET()
                .build()

            val response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(),
            )

            if (response.statusCode() !in SUCCESS_RANGE) return null

            json.decodeFromString<GitHubReleaseDto>(response.body())
        }.onFailure {
            AppLog.w(
                it,
                "Desktop update check failed.",
            )
        }.getOrNull()
    }

    private suspend fun downloadTo(
        url: String,
        destination: File,
        onProgress: (bytesDownloaded: Long, totalBytes: Long?) -> Unit,
    ): Boolean {
        return runCatchingCancellable {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(
                    "User-Agent",
                    USER_AGENT,
                )
                .GET()
                .build()

            val response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofInputStream(),
            )

            response.body().use { input ->
                if (response.statusCode() !in SUCCESS_RANGE) return@runCatchingCancellable false

                val totalBytes = response.headers()
                    .firstValueAsLong("Content-Length")
                    .takeIf { it.isPresent }
                    ?.asLong
                    ?.takeIf { it > 0 }

                destination.outputStream().use { output ->
                    val buffer = ByteArray(COPY_BUFFER_BYTES)
                    var bytesDownloaded = 0L

                    while (true) {
                        currentCoroutineContext().ensureActive()

                        val read = input.read(buffer)

                        if (read < 0) break

                        output.write(
                            buffer,
                            0,
                            read,
                        )

                        bytesDownloaded += read

                        onProgress(
                            bytesDownloaded,
                            totalBytes,
                        )
                    }
                }

                true
            }
        }.onFailure {
            AppLog.w(
                it,
                "Desktop update download failed.",
            )
        }.getOrDefault(false)
    }

    @Serializable
    private data class GitHubReleaseDto(
        @SerialName("tag_name")
        val tagName: String,

        val assets: List<GitHubAssetDto> = emptyList(),
    )

    @Serializable
    private data class GitHubAssetDto(
        val name: String,

        @SerialName("browser_download_url")
        val browserDownloadUrl: String,
    )

    private companion object {
        const val REPOSITORY_PATH = "CinqueIzumi/Softcover"
        const val USER_AGENT = "Softcover-Desktop-Updater"
        const val COPY_BUFFER_BYTES = 64 * 1024
        val SUCCESS_RANGE = 200..299
    }
}
