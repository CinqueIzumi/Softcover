package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.feature.profile.presentation.action.OnLogOutClickAction
import nl.rhaydus.softcover.feature.profile.presentation.action.OnLogOutSheetDismissedAction
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

@Composable
internal fun ProfileSheets(
    state: ProfileUiState,
    runAction: (ProfileAction) -> Unit,
) {
    state.shareSheet.modelOrNull?.let { shareSheet ->
        ProfileShareBottomSheet(
            model = shareSheet,
            runAction = runAction,
        )
    }

    state.logOutSheet.modelOrNull?.let { scaffold ->
        SheetScaffold(
            model = scaffold,
            onEvent = { event ->
                when (event) {
                    SheetScaffoldEvent.PrimaryClicked -> runAction(OnLogOutClickAction())

                    SheetScaffoldEvent.Dismissed -> runAction(OnLogOutSheetDismissedAction())

                    SheetScaffoldEvent.SecondaryClicked,
                    SheetScaffoldEvent.HeaderActionClicked -> Unit
                }
            },
            content = {},
        )
    }
}
