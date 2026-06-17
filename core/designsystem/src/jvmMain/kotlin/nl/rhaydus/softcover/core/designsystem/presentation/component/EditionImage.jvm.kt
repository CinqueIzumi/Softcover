package nl.rhaydus.softcover.core.designsystem.presentation.component

import java.io.File

// Desktop persists covers to the local filesystem (see core:book's EditionImageStorage), so a
// path resolves to a loadable Coil model exactly when the file exists on disk.
actual fun localImageSourceOrNull(localImagePath: String?): String? =
    localImagePath?.takeIf { File(it).exists() }
