package nl.rhaydus.softcover.core.component.field

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/** An outlined field that shows a chosen [value] and opens a picker when tapped; it takes no typed input. */
@Immutable
data class SelectFieldUiModel(
    val key: String,
    val value: String,
    val contentDescription: String,
) {
    companion object : UiModelPreviews<SelectFieldUiModel> {
        override val previews: ImmutableMap<String, SelectFieldUiModel> = persistentMapOf(
            "Currency" to SelectFieldUiModel(
                key = "currency",
                value = "EUR",
                contentDescription = "Currency, EUR",
            ),
            "Long value" to SelectFieldUiModel(
                key = "format",
                value = "Hardcover, signed first edition",
                contentDescription = "Format, Hardcover, signed first edition",
            ),
        )
    }
}
