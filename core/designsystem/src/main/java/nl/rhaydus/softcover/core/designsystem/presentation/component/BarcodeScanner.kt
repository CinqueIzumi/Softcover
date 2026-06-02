package nl.rhaydus.softcover.core.designsystem.presentation.component

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner as MlKitBarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import timber.log.Timber
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Full-bleed CameraX barcode-scanning surface backed by the bundled ML Kit model (no Google Play
 * Services). Constrained to EAN-13 / EAN-8 — the formats printed on physical books — and emits the
 * raw value of the first barcode it reads exactly once via [onIsbnDetected], then stops analysing.
 *
 * The composable is intentionally stateless about resolution: it surfaces a raw string and nothing
 * more. Turning that string into a book — and deciding what an unknown book means — belongs to the
 * call site (see `feature/scan`), never to this leaf.
 */
@SuppressLint("MissingPermission")
@OptIn(markerClass = [ExperimentalGetImage::class])
@Composable
fun BarcodeScanner(
    onIsbnDetected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    val hasEmitted = remember { AtomicBoolean(false) }

    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                )
                .build(),
        )
    }

    DisposableEffect(lifecycleOwner) {
        // Re-arm the emit-once guard so a fresh camera session (e.g. after the owner is recreated)
        // can detect again rather than staying latched from the previous binding.
        hasEmitted.set(false)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { useCase ->
                        useCase.setAnalyzer(analysisExecutor) { imageProxy ->
                            scanFrame(
                                imageProxy = imageProxy,
                                scanner = barcodeScanner,
                                hasEmitted = hasEmitted,
                                onIsbnDetected = onIsbnDetected,
                            )
                        }
                    }

                try {
                    cameraProvider.unbindAll()

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis,
                    )
                } catch (error: Exception) {
                    Timber.e(
                        error,
                        "Failed to bind camera use cases for barcode scanning",
                    )
                }
            },
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }

            barcodeScanner.close()

            analysisExecutor.shutdown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "SCANNING",
                style = MaterialTheme.editorialTypography.eyebrow,
                color = Color.White.copy(alpha = 0.8f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Center a book's barcode",
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.72f)
                .aspectRatio(1.9f)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                ),
        )
    }
}

@ExperimentalGetImage
private fun scanFrame(
    imageProxy: ImageProxy,
    scanner: MlKitBarcodeScanner,
    hasEmitted: AtomicBoolean,
    onIsbnDetected: (String) -> Unit,
) {
    val mediaImage = imageProxy.image

    if (mediaImage == null) {
        imageProxy.close()

        return
    }

    val inputImage = InputImage.fromMediaImage(
        mediaImage,
        imageProxy.imageInfo.rotationDegrees,
    )

    scanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            val isbn = barcodes.firstNotNullOfOrNull { it.rawValue }

            if (isbn != null && hasEmitted.compareAndSet(
                false,
                true,
            )) {
                onIsbnDetected(isbn)
            }
        }
        .addOnFailureListener { Timber.e(
            it,
            "ML Kit failed to process a camera frame",
        ) }
        .addOnCompleteListener { imageProxy.close() }
}
