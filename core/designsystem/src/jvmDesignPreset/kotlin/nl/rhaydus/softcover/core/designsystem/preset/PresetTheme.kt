package nl.rhaydus.softcover.core.designsystem.preset

import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness

/** One theme column of the token file; the default Softcover palette painted at each brightness. */
internal enum class PresetTheme(
    val id: String,
    val displayName: String,
    val brightness: SchemeBrightness,
) {
    LIGHT(id = "light", displayName = "Light", brightness = SchemeBrightness.LIGHT),
    DARK(id = "dark", displayName = "Dark", brightness = SchemeBrightness.DARK),
    DIM(id = "dim", displayName = "Dim", brightness = SchemeBrightness.DIM),
}
