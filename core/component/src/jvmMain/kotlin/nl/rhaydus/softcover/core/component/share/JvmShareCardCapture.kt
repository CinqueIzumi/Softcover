package nl.rhaydus.softcover.core.component.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rhaydus.designsystem.share.ShareCardCapture as FoundationShareCardCapture
import nl.rhaydus.designsystem.share.ShareCardCaptureConfig
import nl.rhaydus.designsystem.share.rememberShareCardCapture as rememberFoundationShareCardCapture
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import java.awt.Image as AwtImage

/**
 * Desktop capture: [share] copies the image to the clipboard and [saveToGallery] opens a native save
 * dialog. Reads and encodes through the foundation capture's layer so `CapturableShareCard` records into it.
 */
internal class JvmShareCardCapture(
    override val foundation: FoundationShareCardCapture,
    private val config: ShareCardCaptureConfig,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ShareCardCapture {
    override val graphicsLayer: GraphicsLayer
        get() = foundation.graphicsLayer

    override suspend fun saveToGallery(displayName: String): SaveOutcome {
        val pngBytes = capturePngBytes()
            ?: return SaveOutcome.Failure(reason = "Failed to encode share card")

        val target = withContext(Dispatchers.Main) { chooseSaveFile(displayName) }
            ?: return SaveOutcome.Cancelled

        return withContext(ioDispatcher) {
            runCatching {
                target.writeBytes(pngBytes)

                SaveOutcome.Saved(
                    identifier = target.absolutePath,
                    displayPath = target.absolutePath,
                )
            }.getOrElse {
                SaveOutcome.Failure(reason = "Failed to write the share card to disk")
            }
        }
    }

    override suspend fun saveToCache(displayName: String): SaveOutcome {
        val pngBytes = capturePngBytes()
            ?: return SaveOutcome.Failure(reason = "Failed to encode share card")

        return withContext(ioDispatcher) {
            runCatching {
                val directory = File(
                    System.getProperty("java.io.tmpdir"),
                    "${config.fileNamePrefix}-$SHARE_CACHE_FOLDER",
                )
                directory.mkdirs()

                val file = File(
                    directory,
                    buildFilename(displayName),
                )
                file.writeBytes(pngBytes)

                SaveOutcome.Cached(identifier = file.absolutePath)
            }.getOrElse {
                SaveOutcome.Failure(reason = "Failed to write the share card to cache")
            }
        }
    }

    override suspend fun share(displayName: String): ShareOutcome {
        val pngBytes = capturePngBytes()
            ?: return ShareOutcome.Failure(reason = "Failed to encode share card")

        return withContext(Dispatchers.Main) {
            runCatching {
                val image = ImageIO.read(ByteArrayInputStream(pngBytes))
                    ?: return@runCatching ShareOutcome.Failure(reason = "Failed to decode share card")

                Toolkit.getDefaultToolkit().systemClipboard.setContents(
                    ImageTransferable(image),
                    null,
                )

                ShareOutcome.CopiedToClipboard
            }.getOrElse {
                ShareOutcome.Failure(reason = "Failed to share")
            }
        }
    }

    // Reads back GPU pixels, so the capture must run on the render (main) thread; PNG encoding then
    // follows inline.
    private suspend fun capturePngBytes(): ByteArray? {
        val skiaBitmap = withContext(Dispatchers.Main) {
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
            )
        ) {
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

        return "${config.fileNamePrefix}-$sanitized.png"
    }

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
actual fun rememberShareCardCapture(config: ShareCardCaptureConfig): ShareCardCapture {
    val foundation = rememberFoundationShareCardCapture(config = config)

    return remember(foundation, config) {
        JvmShareCardCapture(
            foundation = foundation,
            config = config,
        )
    }
}
