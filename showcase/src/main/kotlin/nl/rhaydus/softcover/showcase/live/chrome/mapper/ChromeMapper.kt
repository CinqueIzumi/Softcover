package nl.rhaydus.softcover.showcase.live.chrome.mapper

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.showcase.live.chrome.model.ChromeButtonKey
import nl.rhaydus.softcover.showcase.live.chrome.model.ChromeButtonUiModel
import nl.rhaydus.softcover.showcase.live.chrome.state.ChromeLiveUiState

internal fun chromeLiveState(): ChromeLiveUiState = ChromeLiveUiState(
    cover = CoverUiModel(
        source = null,
        coverlessTitle = "Piranesi",
        variant = CoverVariant.Flat,
    ),
    buttons = persistentListOf(
        ChromeButtonUiModel(
            key = ChromeButtonKey.Share,
            icon = SoftcoverIcon.Share,
            label = "Share",
        ),
        ChromeButtonUiModel(
            key = ChromeButtonKey.Bookmark,
            icon = SoftcoverIcon.BookmarkAdd,
            label = "Bookmark",
        ),
        ChromeButtonUiModel(
            key = ChromeButtonKey.Edit,
            icon = SoftcoverIcon.Edit,
            label = "Edit",
        ),
        ChromeButtonUiModel(
            key = ChromeButtonKey.Appearance,
            icon = SoftcoverIcon.Palette,
            label = "Appearance",
        ),
    ),
    plainHeader = SectionHeaderUiModel.Section(
        eyebrow = "Chrome icon button",
        description = "Above, the buttons sit on cover art behind a scrim. Here they sit on the page. Tap any of them.",
    ),
)

internal fun chromeButtonMessage(key: ChromeButtonKey): String = when (key) {
    ChromeButtonKey.Share -> "Share would open the system sheet here."
    ChromeButtonKey.Bookmark -> "Bookmarked, in a real build."
    ChromeButtonKey.Edit -> "Edit would open the book's page."
    ChromeButtonKey.Appearance -> "Appearance opens its sheet."
}
