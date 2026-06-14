package nl.rhaydus.softcover.core.designsystem.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.koin.compose.koinInject
import java.awt.Image as AwtImage
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.domain.platform.desktopAppDataDirectory

/**
 * Desktop share-card capture. Compose Desktop renders through Skiko, so the capture path mirrors the
 * iOS actual: the [GraphicsLayer] is read on the render thread and encoded to PNG via Skia. Desktop
 * has no system share sheet, so [share] copies the image to the system clipboard (the clean desktop
 * equivalent — the user pastes it wherever they like); [saveToGallery] opens a native save dialog.
 */
internal class JvmShareCardCapture internal constructor(
    override val graphicsLayer: GraphicsLayer,
    private val appDispatchers: AppDispatchers,
) : ShareCardCapture {
    override suspend fun saveToGallery(displayName: String): SaveOutcome {
        val pngBytes = capturePngBytes()
            ?: return SaveOutcome.Failure(reason = "Failed to encode share card")

        val target = withContext(appDispatchers.main) { chooseSaveFile(displayName) }
            ?: return SaveOutcome.Failure(reason = "Save cancelled")

        return withContext(appDispatchers.io) {
            runCatching {
                target.writeBytes(pngBytes)

                SaveOutcome.Saved(
                    identifier = target.absolutePath,
                    displayPath = target.absolutePath,
                )
            }.getOrElse { error ->
                AppLog.e(
                    error,
                    "Failed to save share card",
                )

                SaveOutcome.Failure(reason = "Failed to write the share card to disk")
            }
        }
    }

    override suspend fun saveToCache(displayName: String): SaveOutcome {
        val pngBytes = capturePngBytes()
            ?: return SaveOutcome.Failure(reason = "Failed to encode share card")

        return withContext(appDispatchers.io) {
            runCatching {
                val directory = File(
                    desktopAppDataDirectory(),
                    SHARE_CACHE_FOLDER,
                )
                directory.mkdirs()

                val file = File(
                    directory,
                    buildFilename(displayName),
                )
                file.writeBytes(pngBytes)

                SaveOutcome.Cached(identifier = file.absolutePath)
            }.getOrElse { error ->
                AppLog.e(
                    error,
                    "Failed to cache share card",
                )

                SaveOutcome.Failure(reason = "Failed to write the share card to cache")
            }
        }
    }

    override suspend fun share(displayName: String): ShareOutcome {
        val pngBytes = capturePngBytes()
            ?: return ShareOutcome.Failure(reason = "Failed to encode share card")

        return withContext(appDispatchers.main) {
            runCatching {
                val image = ImageIO.read(ByteArrayInputStream(pngBytes))
                    ?: return@runCatching ShareOutcome.Failure(reason = "Failed to decode share card")

                Toolkit.getDefaultToolkit().systemClipboard.setContents(
                    ImageTransferable(image),
                    null,
                )

                ShareOutcome.Shared
            }.getOrElse { error ->
                AppLog.e(
                    error,
                    "Failed to copy share card to the clipboard",
                )

                ShareOutcome.Failure(reason = "Failed to share")
            }
        }
    }

    // Reads back GPU pixels, so the capture must run on the render (main) thread; PNG encoding then
    // follows inline — cheap enough not to warrant a dispatcher hop.
    private suspend fun capturePngBytes(): ByteArray? {
        val skiaBitmap = withContext(appDispatchers.main) {
            graphicsLayer.toImageBitmap().asSkiaBitmap()
        }

        return Image.makeFromBitmap(skiaBitmap)
            .encodeToData(format = EncodedImageFormat.PNG)
            ?.bytes
    }

    private fun chooseSaveFile(displayName: String): File? {
        val chooser = JFileChooser().apply {
            dialogTitle = "Save share card"
            selectedFile = File(buildFilename(displayName))
            fileFilter = FileNameExtensionFilter(
                "PNG image",
                "png",
            )
        }

        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return null

        val chosen = chooser.selectedFile

        return if (chosen.extension.equals(
            "png",
            ignoreCase = true,
        )) {
            chosen
        } else {
            File("${chosen.absolutePath}.png")
        }
    }

    private fun buildFilename(displayName: String): String {
        val sanitized = displayName.replace(
            Regex("[^A-Za-z0-9-_]"),
            "-",
        )

        return "softcover-$sanitized.png"
    }

    /** Wraps a decoded image as a clipboard transferable for [DataFlavor.imageFlavor]. */
    private class ImageTransferable(
        private val image: AwtImage,
    ) : Transferable {
        override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor)

        override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = flavor == DataFlavor.imageFlavor

        override fun getTransferData(flavor: DataFlavor): Any =
            if (flavor == DataFlavor.imageFlavor) image else throw UnsupportedFlavorException(flavor)
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
        JvmShareCardCapture(
            graphicsLayer = graphicsLayer,
            appDispatchers = appDispatchers,
        )
    }
}
