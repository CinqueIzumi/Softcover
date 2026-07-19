package nl.rhaydus.softcover.core.designsystem.presentation.icon

import org.jetbrains.compose.resources.DrawableResource
import nl.rhaydus.designsystem.icon.RhaydusIconResource
import nl.rhaydus.softcover.core.designsystem.generated.resources.Res
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_account
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_add
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_apk_install
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_arrow_back
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_arrow_drop_down
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_arrow_drop_up
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_barcode_scanner
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_bookmark
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_bookmark_add
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_bookmark_added
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_bookmark_check
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_check
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_check_circle
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_close
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_content_paste
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_date_range
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_delete
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_drag_handle
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_edit
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_explore
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_filter_list
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_headset
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_history
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_info
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_keyboard_arrow_right
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_library_books
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_menu_book
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_more_vert
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_open_in_new
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_palette
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_pause
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_pin
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_play
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_reading
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_search
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_settings
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_share
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_shelf
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_shopping_bag
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_sort
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_star_filled
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_stop
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_storefront
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_view_layout
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_visibility
import nl.rhaydus.softcover.core.designsystem.generated.resources.ic_visibility_off

/**
 * The design system's bundled icon catalog. Each entry maps a stable name to a Compose Multiplatform
 * [DrawableResource], so consumers reference icons through this type (`SoftcoverIcon.Search`) rather
 * than the platform resource system — the icons resolve identically on Android and iOS, and the
 * underlying CMP `Res` stays internal to this module. Wrap
 * in [nl.rhaydus.softcover.core.designsystem.presentation.model.RhaydusIconResource.Drawable] when
 * a content description is needed.
 */
enum class SoftcoverIcon(internal val resource: DrawableResource) {
    Account(Res.drawable.ic_account),
    Add(Res.drawable.ic_add),
    ApkInstall(Res.drawable.ic_apk_install),
    ArrowBack(Res.drawable.ic_arrow_back),
    ArrowDropDown(Res.drawable.ic_arrow_drop_down),
    ArrowDropUp(Res.drawable.ic_arrow_drop_up),
    BarcodeScanner(Res.drawable.ic_barcode_scanner),
    Bookmark(Res.drawable.ic_bookmark),
    BookmarkAdd(Res.drawable.ic_bookmark_add),
    BookmarkAdded(Res.drawable.ic_bookmark_added),
    BookmarkCheck(Res.drawable.ic_bookmark_check),
    Check(Res.drawable.ic_check),
    CheckCircle(Res.drawable.ic_check_circle),
    Close(Res.drawable.ic_close),
    ContentPaste(Res.drawable.ic_content_paste),
    DateRange(Res.drawable.ic_date_range),
    Delete(Res.drawable.ic_delete),
    DragHandle(Res.drawable.ic_drag_handle),
    Edit(Res.drawable.ic_edit),
    Explore(Res.drawable.ic_explore),
    FilterList(Res.drawable.ic_filter_list),
    Headset(Res.drawable.ic_headset),
    History(Res.drawable.ic_history),
    Info(Res.drawable.ic_info),
    KeyboardArrowRight(Res.drawable.ic_keyboard_arrow_right),
    LibraryBooks(Res.drawable.ic_library_books),
    MenuBook(Res.drawable.ic_menu_book),
    MoreVert(Res.drawable.ic_more_vert),
    OpenInNew(Res.drawable.ic_open_in_new),
    Palette(Res.drawable.ic_palette),
    Pause(Res.drawable.ic_pause),
    Pin(Res.drawable.ic_pin),
    Play(Res.drawable.ic_play),
    Reading(Res.drawable.ic_reading),
    Search(Res.drawable.ic_search),
    Settings(Res.drawable.ic_settings),
    Share(Res.drawable.ic_share),
    Shelf(Res.drawable.ic_shelf),
    ShoppingBag(Res.drawable.ic_shopping_bag),
    Sort(Res.drawable.ic_sort),
    StarFilled(Res.drawable.ic_star_filled),
    Stop(Res.drawable.ic_stop),
    Storefront(Res.drawable.ic_storefront),
    ViewLayout(Res.drawable.ic_view_layout),
    Visibility(Res.drawable.ic_visibility),
    VisibilityOff(Res.drawable.ic_visibility_off),
}
