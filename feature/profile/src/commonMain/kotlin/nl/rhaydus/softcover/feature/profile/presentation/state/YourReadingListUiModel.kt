package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel

/**
 * The Your reading list chapter: what is still to come off the reader's list, then the books they set aside.
 *
 * [header], [added], [read] and [timeOnList] load together, and [setAside] and [didNotFinish] load with the
 * did-not-finish stats.
 *
 * @property didNotFinish null when no book was put down in scope, where the [setAside] description says what
 * would show here; the loading stand-in is not null.
 */
internal data class YourReadingListUiModel(
    val header: SectionHeaderUiModel.Section,
    val added: VersusCellUiModel,
    val read: VersusCellUiModel,
    val timeOnList: StatTileUiModel,
    val setAside: SectionHeaderUiModel.Section,
    val didNotFinish: DidNotFinishUiModel?,
)
