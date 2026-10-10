package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.feature.profile.presentation.action.OnLogOutClickAction
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

@Composable
internal fun ProfileSheets(
    state: ProfileUiState,
    showLogOutConfirm: Boolean,
    onLogOutDismiss: () -> Unit,
    runAction: (ProfileAction) -> Unit,
) {
    val shareSheet = state.shareSheet

    if (state.isShareSheetVisible && shareSheet != null) {
        ProfileShareBottomSheet(
            model = shareSheet,
            runAction = runAction,
        )
    }

    if (showLogOutConfirm) {
        SheetScaffold(
            model = state.logOutSheetScaffold,
            onEvent = { event ->
                when (event) {
                    SheetScaffoldEvent.PrimaryClicked -> runAction(OnLogOutClickAction())

                    SheetScaffoldEvent.Dismissed -> onLogOutDismiss()

                    SheetScaffoldEvent.SecondaryClicked,
                    SheetScaffoldEvent.HeaderActionClicked -> Unit
                }
            },
            content = {},
        )
    }
}
