package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.share.ReadingLifeShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCardSheetContent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

@Composable
internal fun ProfileShareBottomSheet(
    model: ReadingLifeShareCardUiModel,
    scaffold: SheetScaffoldUiModel,
    onDismissRequest: () -> Unit,
) {
    ShareCardSheetContent(
        card = model,
        scaffold = scaffold,
        displayName = "${model.readerName}-reading-life",
        onDismissRequest = onDismissRequest,
        previewMaxWidth = 240.dp,
        previewMaxHeight = 420.dp,
    )
}
