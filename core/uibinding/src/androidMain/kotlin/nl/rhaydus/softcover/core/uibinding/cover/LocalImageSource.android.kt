package nl.rhaydus.softcover.core.uibinding.cover

import java.io.File

actual fun localImageSourceOrNull(localImagePath: String?): String? =
    localImagePath?.takeIf { File(it).exists() }
