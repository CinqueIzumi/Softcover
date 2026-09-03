package nl.rhaydus.softcover.core.component.verdict

import androidx.compose.ui.unit.dp

/**
 * The treatment [VerdictSheet]'s `cover` slot expects its image to be drawn with.
 *
 * The sheet takes the cover as a slot because *resolving* a book's cover is the app's job (R4), but
 * the slot is still part of the sheet's anatomy — so its metrics belong here rather than being
 * re-decided by each caller. Both features that raise the sheet render the identical jacket, and
 * before these were shared one of them had extracted a single constant while the other left the
 * values inline: the same treatment, with nothing keeping the two in step.
 *
 * A caller passes these to whatever image component it uses. Only the *shadow colour's* alpha lives
 * here rather than a resolved colour, because the colour itself comes from the caller's theme.
 */
object VerdictSheetCoverDefaults {
    val CornerRadius = 16.dp

    val Elevation = 6.dp

    const val SHADOW_ALPHA = 0.35f
}
