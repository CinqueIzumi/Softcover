package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent

@Composable
internal fun MoodChoices(
    model: MoodChoicesUiModel,
    onChipClick: (String) -> Unit,
    onOwnWordEvent: (TextFieldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHaptics.current

    Column(modifier = modifier) {
        ChipFlow(
            set = model.chips,
            onEvent = { event, key ->
                if (event is ChipEvent.Clicked) {
                    haptics.select()
                    onChipClick(key)
                }
            },
        )

        FeelingReveal(visible = model.ownWordOpen) {
            Column {
                Spacer(modifier = Modifier.height(Spacing.md))

                TextField(
                    model = model.ownWord,
                    onEvent = onOwnWordEvent,
                )
            }
        }
    }
}
