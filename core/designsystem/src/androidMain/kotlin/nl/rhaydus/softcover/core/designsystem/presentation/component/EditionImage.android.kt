package nl.rhaydus.softcover.core.designsystem.presentation.component

import java.io.File

actual fun localImageSourceOrNull(localImagePath: String?): String? =
    localImagePath?.takeIf { File(it).exists() }
