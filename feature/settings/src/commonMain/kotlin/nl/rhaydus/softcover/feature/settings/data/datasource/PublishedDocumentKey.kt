package nl.rhaydus.softcover.feature.settings.data.datasource

/**
 * The documents published from the repo root. Each constant carries its own storage key, remote path
 * and bundled resource path as explicit strings: the storage key is persisted, so it must not follow a
 * rename of the constant.
 */
internal enum class PublishedDocumentKey(
    val storageKey: String,
    val rawUrl: String,
    val bundledPath: String,
) {
    ROADMAP(
        storageKey = "roadmap",
        rawUrl = "https://raw.githubusercontent.com/CinqueIzumi/Softcover/main/ROADMAP.md",
        bundledPath = "files/roadmap.md",
    ),
    CHANGELOG(
        storageKey = "changelog",
        rawUrl = "https://raw.githubusercontent.com/CinqueIzumi/Softcover/main/CHANGELOG.md",
        bundledPath = "files/changelog.md",
    ),
}
