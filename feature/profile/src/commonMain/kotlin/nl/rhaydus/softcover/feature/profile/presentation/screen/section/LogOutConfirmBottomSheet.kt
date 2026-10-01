package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

@Composable
internal fun LogOutConfirmBottomSheet(
    scaffold: SheetScaffoldUiModel,
    onLogOutClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    SheetScaffold(
        model = scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> onLogOutClick()

                SheetScaffoldEvent.Dismissed -> onDismissRequest()

                SheetScaffoldEvent.TopActionClicked,
                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked,
                -> Unit
            }
        },
        content = {},
    )
}
