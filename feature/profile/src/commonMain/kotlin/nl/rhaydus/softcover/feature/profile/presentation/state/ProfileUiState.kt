package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.core.component.share.ShareCardSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.readingSummaryUiModel
import nl.rhaydus.toad.UiState

internal data class ProfileUiState(
    val userProfileData: UserProfileData? = null,
    val readingLife: ReadingLife? = null,

    val shareSheet: SheetSession<ShareCardSheetUiModel, Nothing> = SheetSession.Closed,

    val isShareSheetRequested: Boolean = false,
    val shareCapture: ShareCaptureState = ShareCaptureState(),
    val isLoading: Boolean = true,

    /** Mapped by `ReadingSummaryCollector` off [readingLife] and the current year. */
    val readingSummary: ReadingSummaryUiModel = readingSummaryUiModel(
        readingLife = null,
        currentYear = 0,
    ),

    val theReaderHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "Reader"),
    val logOutSheet: SheetSession<SheetScaffoldUiModel, LogOut> = SheetSession.Closed,

    /** Both bars carry no title — the page's own masthead names it. */
    val topBar: TopBarUiModel = TopBarUiModel(),
) : UiState {
    val canShare: Boolean get() = readingLife != null && userProfileData != null

    val isReadingLifeLoading: Boolean
        get() = isLoading || readingLife == null
}
