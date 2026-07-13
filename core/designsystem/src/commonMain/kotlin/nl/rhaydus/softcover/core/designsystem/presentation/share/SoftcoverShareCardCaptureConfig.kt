package nl.rhaydus.softcover.core.designsystem.presentation.share

import nl.rhaydus.designsystem.share.ShareCardCaptureConfig

/**
 * Softcover's brand configuration for the foundation `rememberShareCardCapture` seam: the exported-PNG
 * file-name prefix, the gallery album images land in, and the Android `FileProvider` authority used to
 * grant read access on the share intent (declared as `${applicationId}.shareprovider` in the app manifest).
 * Supplied once here and passed at every capture call site so the foundation seam stays brand-agnostic.
 *
 * The authority is a literal because the foundation `ShareCardCaptureConfig` is consumed from `commonMain`
 * (no Android `Context` to read `packageName` from). It must stay in sync with the app's `applicationId` —
 * if a build type ever adds an `applicationIdSuffix`, update this to match the manifest's resolved authority.
 */
val softcoverShareCardCaptureConfig: ShareCardCaptureConfig = ShareCardCaptureConfig(
    fileNamePrefix = "softcover",
    galleryAlbum = "Softcover",
    androidFileProviderAuthority = "nl.rhaydus.softcover.shareprovider",
)
