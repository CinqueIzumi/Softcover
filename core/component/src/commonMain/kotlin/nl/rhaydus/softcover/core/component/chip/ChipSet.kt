package nl.rhaydus.softcover.core.component.chip

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * A chip list paired with the payload each [ChipUiModel.key] resolves to, so the two can never
 * drift apart. An action resolves a tapped key back through the set it came from (R9).
 */
@Immutable
data class ChipSet<out P>(
    val chips: ImmutableList<ChipUiModel> = persistentListOf(),
    val payloadByKey: ImmutableMap<String, P> = persistentMapOf(),
) {
    val isEmpty: Boolean get() = chips.isEmpty()

    operator fun get(key: String): P? = payloadByKey[key]

    companion object : UiModelPreviews<ChipSet<String>> {
        override val previews: ImmutableMap<String, ChipSet<String>> = persistentMapOf(
            "Filter set" to listOf(
                ChipUiModel(
                    key = "fantasy",
                    label = "Fantasy",
                    selected = true,
                ) to "fantasy",
                ChipUiModel(
                    key = "mystery",
                    label = "Cozy mystery",
                ) to "mystery",
                ChipUiModel(
                    key = "classics",
                    label = "Nineteenth-century classics",
                ) to "classics",
                ChipUiModel(
                    key = "memoir",
                    label = "Memoir",
                ) to "memoir",
            ).toChipSet(),
            "Removable tags" to listOf(
                ChipUiModel(
                    key = "second-helpings",
                    label = "Second helpings",
                    tone = ChipTone.Container,
                    trailing = ChipTrailing.Icon(
                        icon = SoftcoverIcon.Close,
                        description = "Remove Second helpings",
                    ),
                ) to "second-helpings",
                ChipUiModel(
                    key = "slow-burn",
                    label = "Slow burn",
                    tone = ChipTone.Container,
                    trailing = ChipTrailing.Icon(
                        icon = SoftcoverIcon.Close,
                        description = "Remove Slow burn",
                    ),
                ) to "slow-burn",
            ).toChipSet(),
        )
    }
}

fun <P> Iterable<Pair<ChipUiModel, P>>.toChipSet(): ChipSet<P> =
    ChipSet(
        chips = map { it.first }.toImmutableList(),
        payloadByKey = associate { it.first.key to it.second }.toImmutableMap(),
    )
