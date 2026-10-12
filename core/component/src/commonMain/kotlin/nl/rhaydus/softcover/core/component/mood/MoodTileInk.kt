package nl.rhaydus.softcover.core.component.mood

/**
 * The tile's colour look; the mapper cycles through the four by position.
 *
 * [Cosy], [Dread] and [HeartWrench] are fixed inks that read the same in light and dark; [Neutral] uses
 * ordinary theme roles, so a grid keeps one tonal tile among the inked ones.
 */
enum class MoodTileInk {
    Cosy,
    Dread,
    Neutral,
    HeartWrench,
}
