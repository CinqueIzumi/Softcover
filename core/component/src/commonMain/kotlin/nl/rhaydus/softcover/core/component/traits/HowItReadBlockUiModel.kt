package nl.rhaydus.softcover.core.component.traits

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/** What [HowItReadBlock] renders: an invitation to answer ([Empty]) or the reader's answers ([Filled]). */
@Immutable
sealed interface HowItReadBlockUiModel {
    /** Nothing answered yet; the whole panel invites an edit. */
    data class Empty(val invitation: String) : HowItReadBlockUiModel

    /**
     * At least one answer. [summary] is the collapsed line; [rows] open beneath it while [expanded].
     * [toggleLabel] describes what a tap on the summary does, for screen readers.
     */
    data class Filled(
        val summary: String,
        val rows: ImmutableList<HowItReadRowUiModel>,
        val expanded: Boolean,
        val toggleLabel: String,
        val editLabel: String,
    ) : HowItReadBlockUiModel

    companion object : UiModelPreviews<HowItReadBlockUiModel> {
        private val answers: ImmutableList<HowItReadRowUiModel> = persistentListOf(
            HowItReadRowUiModel.Text(
                label = "Pace",
                value = "Variable",
            ),
            HowItReadRowUiModel.Scale(
                label = "Plot or character",
                value = "Mostly character",
                position = 0.8f,
                startLabel = "Plot",
                centreLabel = "A mix",
                endLabel = "Character",
            ),
            HowItReadRowUiModel.Text(
                label = "The characters were",
                value = "Likeable, believable",
            ),
            HowItReadRowUiModel.Text(
                label = "Writing style",
                value = "Demanding, poetic",
            ),
        )

        private fun filled(expanded: Boolean): Filled = Filled(
            summary = "Variable pace, mostly character, demanding writing",
            rows = answers,
            expanded = expanded,
            toggleLabel = if (expanded) "Hide how it read" else "Show how it read",
            editLabel = "Edit",
        )

        /** One fixture per branch the block renders: empty, filled collapsed, and filled with its rows open. */
        override val previews: ImmutableMap<String, HowItReadBlockUiModel> = persistentMapOf(
            "Empty" to Empty(invitation = "Add the pace, the characters and the writing."),
            "Filled, collapsed" to filled(expanded = false),
            "Filled, expanded" to filled(expanded = true),
        )
    }
}
