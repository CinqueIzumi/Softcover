package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/** @property titleIcon Decorative; screen readers skip it, so the title must carry the meaning. */
@Immutable
data class BookCardContent(
    val title: String,
    val eyebrow: BookCardEyebrow? = null,
    val titleIcon: SoftcoverIcon? = null,
    val byline: String? = null,
    val supportingLines: ImmutableList<ImmutableList<BookCardLinePart>> = persistentListOf(),
    val titleTransitionKey: String? = null,
)
