package nl.rhaydus.softcover.feature.lists.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.control.SegmentedControl
import nl.rhaydus.softcover.core.component.control.SegmentedControlEvent
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerCenter
import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.lists.presentation.action.CreateListAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnNameChangedAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnPrivacySelectedAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnSubmitAction
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState

/**
 * The "List Creation 1a" title page: a centred spread — eyebrow, a hairline rule broken by a quote
 * glyph, the name huge and borderless, a tappable public/private sentence, and a single filled
 * "Create list" pill. No field chrome anywhere; naming a list is a ten-second act of authorship, not
 * a form. This is a **documented exception** to the standard modal-sheet header (design-system.md §3.5
 * / §5 "Editorial section") — see the design-system doc's "Title-page sheet composition" pattern entry.
 */
@Composable
internal fun CreateListSheetContent(
    state: CreateListUiState,
    runAction: (CreateListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val submit: () -> Unit = {
        if (state.canSubmit) {
            keyboardController?.hide()

            runAction(OnSubmitAction())
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
            .imePadding()
            .padding(
                top = 8.dp,
                start = 28.dp,
                end = 28.dp,
                bottom = 28.dp,
            ),
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        SoftcoverText(
            text = "A NEW LIST",
            style = MaterialTheme.editorialTypography.eyebrow.copy(letterSpacing = 3.sp),
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Divider(model = DividerUiModel.Horizontal(center = DividerCenter.QuoteMark))

        Spacer(modifier = Modifier.height(30.dp))

        TextField(
            model = state.nameField,
            onEvent = { event ->
                when (event) {
                    is TextFieldEvent.ValueChanged -> runAction(OnNameChangedAction(newName = event.text))

                    is TextFieldEvent.Submitted -> submit()

                    is TextFieldEvent.TrailingClicked -> Unit
                }
            },
        )

        Spacer(modifier = Modifier.height(30.dp))

        Divider(model = DividerUiModel.Horizontal(inset = 40.dp))

        Spacer(modifier = Modifier.height(30.dp))

        SegmentedControl(
            model = state.privacyToggle,
            onEvent = { event ->
                when (event) {
                    is SegmentedControlEvent.Selected -> runAction(OnPrivacySelectedAction(privacy = event.key))
                }
            },
        )

        Spacer(modifier = Modifier.height(34.dp))

        RhaydusButton(
            label = "Create list",
            style = ButtonStyle.FILLED,
            size = ButtonSize.M,
            enabled = state.canSubmit,
            onClick = submit,
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor(),
        )
    }
}
