package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/**
 * The Your reading list chapter: the books the reader put down, for now its only section.
 *
 * @property didNotFinish null when no book was put down in scope, where the header's description says what
 * would show here; the loading stand-in is not null.
 */
internal data class YourReadingListUiModel(
    val header: SectionHeaderUiModel.Section,
    val didNotFinish: DidNotFinishUiModel?,
    val isLoading: Boolean,
)
