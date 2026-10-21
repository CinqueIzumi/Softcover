package nl.rhaydus.softcover.showcase.live

import cafe.adriel.voyager.core.screen.Screen
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.showcase.live.callout.screen.CalloutLiveScreen
import nl.rhaydus.softcover.showcase.live.celebration.screen.CelebrationLiveScreen
import nl.rhaydus.softcover.showcase.live.chip.screen.ChipLiveScreen
import nl.rhaydus.softcover.showcase.live.chrome.screen.ChromeLiveScreen
import nl.rhaydus.softcover.showcase.live.control.screen.ControlLiveScreen
import nl.rhaydus.softcover.showcase.live.dialog.screen.DialogLiveScreen
import nl.rhaydus.softcover.showcase.live.field.screen.FieldLiveScreen
import nl.rhaydus.softcover.showcase.live.lists.screen.ListsLiveScreen
import nl.rhaydus.softcover.showcase.live.progress.screen.ProgressLiveScreen
import nl.rhaydus.softcover.showcase.live.quote.screen.QuoteLiveScreen
import nl.rhaydus.softcover.showcase.live.selection.screen.SelectionLiveScreen
import nl.rhaydus.softcover.showcase.live.share.screen.ShareLiveScreen
import nl.rhaydus.softcover.showcase.live.sheet.screen.SheetLiveScreen
import nl.rhaydus.softcover.showcase.live.sidebar.screen.SidebarLiveScreen
import nl.rhaydus.softcover.showcase.live.snackbar.screen.SnackbarLiveScreen
import nl.rhaydus.softcover.showcase.live.topbar.screen.TopbarLiveScreen
import nl.rhaydus.softcover.showcase.live.verdict.screen.VerdictLiveScreen

internal fun GalleryFamily.liveScreen(): Screen? = when (this) {
    GalleryFamily.SHEET -> SheetLiveScreen()
    GalleryFamily.PROGRESS -> ProgressLiveScreen()
    GalleryFamily.VERDICT -> VerdictLiveScreen()
    GalleryFamily.QUOTE -> QuoteLiveScreen()
    GalleryFamily.LISTS -> ListsLiveScreen()
    GalleryFamily.SHARE -> ShareLiveScreen()
    GalleryFamily.DIALOG -> DialogLiveScreen()
    GalleryFamily.SNACKBAR -> SnackbarLiveScreen()
    GalleryFamily.CELEBRATION -> CelebrationLiveScreen()
    GalleryFamily.CALLOUT -> CalloutLiveScreen()
    GalleryFamily.TOPBAR -> TopbarLiveScreen()
    GalleryFamily.SIDEBAR -> SidebarLiveScreen()
    GalleryFamily.CHIP -> ChipLiveScreen()
    GalleryFamily.CONTROL -> ControlLiveScreen()
    GalleryFamily.FIELD -> FieldLiveScreen()
    GalleryFamily.SELECTION -> SelectionLiveScreen()
    GalleryFamily.CHROME -> ChromeLiveScreen()
    else -> null
}

internal fun GalleryFamily.hasLiveScreen(): Boolean = liveScreen() != null
