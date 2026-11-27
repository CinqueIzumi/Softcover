package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.field.TextFieldBody
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.selection.SelectionCheck

@Composable
internal fun SheetInputRow(
    model: SheetInputRowUiModel,
    onEvent: (TextFieldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    Column(modifier = modifier) {
        Divider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    role = Role.RadioButton
                    selected = model.selected
                    contentDescription = model.field.text.ifEmpty { model.field.placeholder }

                    onClick {
                        focusRequester.requestFocus()
                        true
                    }
                }
                .padding(vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            val ink by animateColorAsState(
                targetValue = if (model.selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>(),
            )

            TextFieldBody(
                model = model.field,
                onEvent = onEvent,
                ink = ink,
                focusRequester = focusRequester,
                modifier = Modifier.weight(1f),
            )

            SelectionCheck(selected = model.selected)
        }
    }
}
