package nl.rhaydus.softcover.core.designsystem.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import kotlin.coroutines.resume
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.koin.compose.koinInject
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController
import nl.rhaydus.softcover.core.domain.model.AppDispatchers

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class IosShareCardCapture internal constructor(
    override val graphicsLayer: GraphicsLayer,
    private val appDispatchers: AppDispatchers,
) : ShareCardCapture {
    // TODO(iOS): gallery save needs PHPhotoLibrary + NSPhotoLibraryAddUsageDescription; revisit with the permission requester.
    override suspend fun saveToGallery(displayName: String): SaveOutcome =
        SaveOutcome.Failure(reason = "gallery save not implemented on iOS")

    override suspend fun saveToCache(displayName: String): SaveOutcome {
        val pngBytes = capturePngBytes()
            ?: return SaveOutcome.Failure(reason = "Failed to encode share card")

        return withContext(appDispatchers.io) {
            val directory = "${cachesDirectory()}/$SHARE_CACHE_FOLDER"
            NSFileManager.defaultManager.createDirectoryAtPath(
                path = directory,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )

            val path = "$directory/${buildFilename(displayName = displayName)}"
            val written = pngBytes.toNSData().writeToFile(
                path = path,
                atomically = true,
            )

            if (written) {
                SaveOutcome.Cached(identifier = path)
            } else {
                SaveOutcome.Failure(reason = "Failed to write share card to cache")
            }
        }
    }

    override suspend fun share(displayName: String): ShareOutcome {
        val pngBytes = capturePngBytes()
            ?: return ShareOutcome.Failure(reason = "Failed to encode share card")

        val image = UIImage.imageWithData(data = pngBytes.toNSData())
            ?: return ShareOutcome.Failure(reason = "Failed to build UIImage")

        return withContext(appDispatchers.main) {
            val presenter = topMostViewController()
                ?: return@withContext ShareOutcome.Failure(reason = "No root view controller to present from")

            // Suspend until the native share sheet is dismissed — otherwise the caller's bottom
            // sheet would collapse out from under a still-visible UIActivityViewController.
            suspendCancellableCoroutine<ShareOutcome> { continuation ->
                val activityController = UIActivityViewController(
                    activityItems = listOf(image),
                    applicationActivities = null,
                )

                activityController.completionWithItemsHandler = { _, _, _, _ ->
                    continuation.resume(ShareOutcome.Shared)
                }

                // iPad presents share sheets in a popover; anchor it to the presenter's view.
                activityController.popoverPresentationController?.sourceView = presenter.view

                presenter.presentViewController(
                    activityController,
                    animated = true,
                    completion = null,
                )
            }
        }
    }

    // Reads back GPU pixels, so the capture must run on the main thread regardless of the caller's
    // dispatcher; PNG encoding is cheap enough to follow inline.
    private suspend fun capturePngBytes(): ByteArray? {
        val skiaBitmap = withContext(appDispatchers.main) {
            graphicsLayer.toImageBitmap().asSkiaBitmap()
        }

        return Image.makeFromBitmap(skiaBitmap)
            .encodeToData(format = EncodedImageFormat.PNG)
            ?.bytes
    }

    private fun buildFilename(displayName: String): String {
        val sanitized = displayName.replace(
            Regex("[^A-Za-z0-9-_]"),
            "-",
        )

        return "softcover-$sanitized.png"
    }

    private fun cachesDirectory(): String =
        NSSearchPathForDirectoriesInDomains(
            directory = NSCachesDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true,
        ).first() as String

    private fun topMostViewController(): UIViewController? {
        // `keyWindow` is deprecated and returns nil under multi-scene iPad; resolve the foreground
        // scene's window first, falling back to the legacy accessor on older single-scene setups.
        val window = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?.windows
            ?.filterIsInstance<UIWindow>()
            ?.firstOrNull()
            ?: UIApplication.sharedApplication.keyWindow

        var controller = window?.rootViewController

        while (controller?.presentedViewController != null) {
            controller = controller.presentedViewController
        }

        return controller
    }

    private fun ByteArray.toNSData(): NSData =
        if (isEmpty()) {
            NSData()
        } else {
            usePinned { pinned ->
                NSData.create(
                    bytes = pinned.addressOf(0),
                    length = size.convert(),
                )
            }
        }

    private companion object {
        const val SHARE_CACHE_FOLDER = "share"
    }
}

@Composable
actual fun rememberShareCardCapture(): ShareCardCapture {
    val graphicsLayer = rememberGraphicsLayer()
    val appDispatchers = koinInject<AppDispatchers>()

    return remember(graphicsLayer, appDispatchers) {
        IosShareCardCapture(
            graphicsLayer = graphicsLayer,
            appDispatchers = appDispatchers,
        )
    }
}
