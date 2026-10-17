package nl.rhaydus.softcover.feature.widget.domain.model

/**
 * The settings an instance was saved with, emitted only when they differ from the stored ones.
 *
 * Deliberately a plain class: consumers dedupe by reference identity, so two saves of equal settings are still
 * two changes. Do not make it a `data class`, which would collapse them.
 */
internal class RandomPickSettingsChange(
    val appWidgetId: Int,
    val settings: RandomPickSettings,
)
