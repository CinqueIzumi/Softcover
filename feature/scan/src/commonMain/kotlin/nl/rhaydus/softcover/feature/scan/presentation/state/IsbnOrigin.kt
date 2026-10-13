package nl.rhaydus.softcover.feature.scan.presentation.state

/** Where a submitted ISBN came from, which decides how an invalid one is reported. */
internal enum class IsbnOrigin {
    Camera,
    Manual,
}
