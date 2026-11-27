package nl.rhaydus.softcover.feature.profile.presentation.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/**
 * The Recommenders and spending chapter: what the books read in scope cost, the lead currency first.
 *
 * @property emptyLine set when no book in scope has a price, where [header] carries no description and
 * [spent] and [typical] are null and [blocks] is empty; the loading stand-in has none.
 * @property blocks the lead currency's split by source, then one block per other currency.
 */
@Immutable
internal data class SpendingChapterUiModel(
    val header: SectionHeaderUiModel.Section,
    val emptyLine: String?,
    val spent: VersusCellUiModel?,
    val typical: VersusCellUiModel?,
    val blocks: ImmutableList<SpendBlockUiModel>,
)
