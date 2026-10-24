package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.traits.HowItReadBlockUiModel
import nl.rhaydus.softcover.core.component.traits.HowItReadRowUiModel
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.TraitAnswer

private const val MOSTLY_PLOT_FROM = 21
private const val MIX_FROM = 41
private const val MOSTLY_CHARACTER_FROM = 60
private const val CHARACTER_DRIVEN_FROM = 80
private val ACRONYM_LABELS = setOf("BIPOC", "LGBTQ+")

internal fun BookTraits.toHowItReadBlock(expanded: Boolean): HowItReadBlockUiModel {
    if (isEmpty) return HowItReadBlockUiModel.Empty(invitation = "Add the pace, the characters and the writing.")

    return HowItReadBlockUiModel.Filled(
        summary = summary(),
        rows = answeredRows().toImmutableList(),
        expanded = expanded,
        toggleLabel = if (expanded) "Hide how it read" else "Show how it read",
        editLabel = "Edit",
    )
}

private fun BookTraits.answeredRows(): List<HowItReadRowUiModel> = listOfNotNull(
    pace?.let {
        HowItReadRowUiModel.Text(
            label = "Pace",
            value = it.toLabel(),
        )
    },
    plotCharacterBalance?.let {
        HowItReadRowUiModel.Scale(
            label = "Plot or character",
            value = it.toBalanceBucket(),
            position = it / BALANCE_MAX.toFloat(),
            startLabel = "Plot",
            centreLabel = "A mix",
            endLabel = "Character",
        )
    },
    characterLabels(answer = TraitAnswer.YES).toRow(label = "Characters"),
    characterLabels(answer = TraitAnswer.COMPLICATED).toRow(label = "Complicated"),
    writingLabels().toRow(label = "Writing"),
)

/**
 * Pace, balance bucket and writing style when any is answered; otherwise the first answered of the
 * characters, the complicated characters and the writing flags.
 */
private fun BookTraits.summary(): String {
    val parts = listOfNotNull(
        pace?.let { "${it.toLabel()} pace" },
        plotCharacterBalance?.toBalanceBucket(),
        writingStyle?.let { "${it.toLabel()} writing" },
    )

    return if (parts.isEmpty()) {
        listOf(
            characterLabels(answer = TraitAnswer.YES),
            characterLabels(answer = TraitAnswer.COMPLICATED),
            writingLabels(),
        )
            .firstOrNull { it.isNotEmpty() }
            .orEmpty()
            .toValueList()
    } else {
        parts.joinToString(", ").lowercase().replaceFirstChar { it.uppercase() }
    }
}

private fun BookTraits.characterLabels(answer: TraitAnswer): List<String> = characters
    .filterValues { it == answer }
    .keys
    .sorted()
    .map { it.toLabel() }

private fun BookTraits.writingLabels(): List<String> = listOfNotNull(writingStyle?.toLabel()) +
    writingFlags.sorted().map { it.toLabel() }

private fun List<String>.toRow(label: String): HowItReadRowUiModel? = takeIf { it.isNotEmpty() }
    ?.let {
        HowItReadRowUiModel.Text(
            label = label,
            value = it.toValueList(),
        )
    }

/** Comma-joined, the first word capitalised and the rest lower case but for the acronyms. */
private fun List<String>.toValueList(): String = mapIndexed { index, label ->
    if (index == 0 || label in ACRONYM_LABELS) label else label.lowercase()
}.joinToString(", ")

private fun Int.toBalanceBucket(): String = when {
    this < MOSTLY_PLOT_FROM -> "Plot-driven"
    this < MIX_FROM -> "Mostly plot"
    this < MOSTLY_CHARACTER_FROM -> "A mix"
    this < CHARACTER_DRIVEN_FROM -> "Mostly character"
    else -> "Character-driven"
}
