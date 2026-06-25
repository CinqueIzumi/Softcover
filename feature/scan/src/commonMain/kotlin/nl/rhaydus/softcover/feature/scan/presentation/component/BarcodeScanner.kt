package nl.rhaydus.softcover.feature.scan.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Full-bleed barcode-scanning surface. Constrained to EAN-13 / EAN-8 — the formats printed on
 * physical books — and emits the raw value of the first barcode it reads exactly once via
 * [onIsbnDetected].
 *
 * The composable is intentionally stateless about resolution: it surfaces a raw string and nothing
 * more. Turning that string into a book — and deciding what an unknown book means — belongs to the
 * call site (the scan ScreenModel), never to this leaf.
 */
@Composable
expect fun BarcodeScanner(
    onIsbnDetected: (String) -> Unit,
    modifier: Modifier = Modifier,
)
