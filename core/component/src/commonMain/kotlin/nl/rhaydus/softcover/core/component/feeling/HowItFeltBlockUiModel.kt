package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * What [HowItFeltBlock] renders. With no [moods] and no [moments] the block shows [invitation] under its
 * header instead of rows; [momentsTitle] only shows once there is a moment.
 */
@Immutable
data class HowItFeltBlockUiModel(
    val title: String,
    val editLabel: String,
    val invitation: String,
    val moods: ImmutableList<HowItFeltMoodUiModel>,
    val momentsTitle: String,
    val moments: ImmutableList<HowItFeltMomentUiModel>,
    val caption: String,
) {
    val isEmpty: Boolean get() = moods.isEmpty() && moments.isEmpty()

    companion object : UiModelPreviews<HowItFeltBlockUiModel> {
        private val empty = HowItFeltBlockUiModel(
            title = "How it felt",
            editLabel = "Edit",
            invitation = "Tap Edit to say how it felt, in your own words.",
            moods = persistentListOf(),
            momentsTitle = "Along the way",
            moments = persistentListOf(),
            caption = "Added when you update your progress.",
        )

        override val previews: ImmutableMap<String, HowItFeltBlockUiModel> = persistentMapOf(
            "Empty" to empty,
            "Filled" to empty.copy(
                moods = persistentListOf(
                    HowItFeltMoodUiModel(
                        key = "tense",
                        label = "Tense",
                        strength = 80,
                    ),
                    HowItFeltMoodUiModel(
                        key = "hopeful",
                        label = "Hopeful",
                        strength = 55,
                    ),
                    HowItFeltMoodUiModel(
                        key = "funny",
                        label = "Funny",
                        strength = 20,
                    ),
                ),
                moments = persistentListOf(
                    HowItFeltMomentUiModel(
                        id = "1",
                        at = "18%",
                        label = "Curious",
                        strength = 50,
                    ),
                    HowItFeltMomentUiModel(
                        id = "2",
                        at = "38%",
                        label = "Tense",
                        strength = 70,
                    ),
                    HowItFeltMomentUiModel(
                        id = "3",
                        at = "62%",
                        label = "Wrecked",
                        strength = 90,
                    ),
                ),
            ),
            "Strength unset, moments only" to empty.copy(
                moments = persistentListOf(
                    HowItFeltMomentUiModel(
                        id = "1",
                        at = "41%",
                        label = "Gripped",
                        strength = null,
                    ),
                ),
            ),
        )
    }
}

/** A mood the reader gave the whole book. A null [strength] draws no number and no fill. */
@Immutable
data class HowItFeltMoodUiModel(
    val key: String,
    val label: String,
    val strength: Int?,
)

/** A mood the reader marked at one point in the book; [at] is the position, such as "62%". */
@Immutable
data class HowItFeltMomentUiModel(
    val id: String,
    val at: String,
    val label: String,
    val strength: Int?,
)
