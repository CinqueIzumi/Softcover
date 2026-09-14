package nl.rhaydus.softcover.core.uibinding.cover

/**
 * Returns [localImagePath] when it points at a persisted local cover file that exists on disk (a
 * model Coil can load directly), or null otherwise. Android and desktop check the filesystem; iOS
 * has no persisted-cover support yet.
 */
expect fun localImageSourceOrNull(localImagePath: String?): String?
