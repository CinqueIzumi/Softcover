package nl.rhaydus.softcover.feature.lists.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.component.control.SegmentedControl
import nl.rhaydus.softcover.core.component.control.SegmentedControlEvent
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerCenter
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.lists.presentation.action.CreateListAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnNameChangedAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnNameShakeEndedAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnPrivacySelectedAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnSubmitAction
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState

/**
 * The title page body, a centred spread: a hairline rule broken by a quote glyph,
 * the name huge and borderless, and a tappable public/private sentence; the single "Create list"
 * button is the scaffold's footer, and the scaffold renders no header because the eyebrow sits in this body.
 * No field chrome anywhere; naming a list is a ten-second act of authorship, not a form. This is a
 * **documented exception** to the standard modal-sheet header (design-system.md §3.5 / §5 "Editorial
 * section"); see the design-system doc's "Title-page sheet composition" pattern entry.
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
            .padding(bottom = Spacing.xs),
    ) {
        Spacer(modifier = Modifier.height(Spacing.sm))

        SoftcoverText(
            text = "A NEW LIST",
            style = MaterialTheme.editorialTypography.eyebrow.copy(letterSpacing = 3.sp),
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Divider(center = DividerCenter.QuoteMark)

        Spacer(modifier = Modifier.height(Spacing.xl))

        TextField(
            model = state.nameField,
            onEvent = { event ->
                when (event) {
                    is TextFieldEvent.ValueChanged -> runAction(OnNameChangedAction(newName = event.text))

                    is TextFieldEvent.Submitted -> submit()

                    is TextFieldEvent.TrailingClicked -> Unit

                    is TextFieldEvent.ShakeEnded -> runAction(OnNameShakeEndedAction)
                }
            },
        )

        Spacer(modifier = Modifier.height(Spacing.xxxl))

        Divider(modifier = Modifier.padding(horizontal = Spacing.x4l))

        Spacer(modifier = Modifier.height(Spacing.xxxl))

        SegmentedControl(
            model = state.privacyToggle,
            onEvent = { event ->
                when (event) {
                    is SegmentedControlEvent.Selected -> runAction(OnPrivacySelectedAction(privacy = event.key))
                }
            },
        )
    }
}
