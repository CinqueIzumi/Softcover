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
    showShareSheet: Boolean,
    showLogOutConfirm: Boolean,
    onShareDismiss: () -> Unit,
    onLogOutDismiss: () -> Unit,
    runAction: (ProfileAction) -> Unit,
) {
    val shareContent = state.readingLifeShareCard

    if (showShareSheet && shareContent != null) {
        ProfileShareBottomSheet(
            model = shareContent,
            scaffold = state.shareSheetScaffold,
            onDismissRequest = onShareDismiss,
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
