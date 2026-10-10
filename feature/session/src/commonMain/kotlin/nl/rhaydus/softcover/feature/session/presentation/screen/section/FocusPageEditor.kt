package nl.rhaydus.softcover.feature.session.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import nl.rhaydus.designsystem.editorial.component.EditorialSuffix
import nl.rhaydus.designsystem.editorial.component.HeroStatNumberField
import nl.rhaydus.designsystem.modifier.shakeOnError
import nl.rhaydus.softcover.core.component.progress.ProgressBar
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.session.presentation.action.FocusModeAction
import nl.rhaydus.softcover.feature.session.presentation.action.OnPageDraftChangedAction
import nl.rhaydus.softcover.feature.session.presentation.action.OnPageFocusGainedAction
import nl.rhaydus.softcover.feature.session.presentation.action.OnPageFocusLostAction
import nl.rhaydus.softcover.feature.session.presentation.action.OnPageShakeEndedAction
import nl.rhaydus.softcover.feature.session.presentation.state.FocusPageEditorUiModel

/**
 * The reading position rendered as an editable hero stat (DS §5) — the same treatment as the
 * progress sheet's page tab: a borderless [HeroStatNumberField] in the stat face over an editorial
 * caption and a progress bar. Tapping it opens the keyboard; the new page commits when the field
 * loses focus (keyboard "Done" or tapping away). Books without a page count (audiobooks / unknown)
 * show a read-only caption instead of the field.
 */
@Composable
internal fun FocusPageEditor(
    pageEditor: FocusPageEditorUiModel,
    pageDraft: TextFieldValue,
    pageDraftRejected: Boolean,
    runAction: (FocusModeAction) -> Unit,
) {
    if (pageEditor.isEditable.not()) {
        EditorialSuffix(text = pageEditor.caption)

        Spacer(modifier = Modifier.height(Spacing.sm))

        ProgressBar(fraction = pageEditor.fraction)

        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        HeroStatNumberField(
            value = pageDraft,
            charCount = 4,
            onValueChange = { runAction(OnPageDraftChangedAction(newValue = it)) },
            onFocusReset = { runAction(OnPageFocusLostAction) },
            onFocusGained = { runAction(OnPageFocusGainedAction) },
            modifier = Modifier.shakeOnError(
                trigger = pageDraftRejected,
                onShakeEnd = { runAction(OnPageShakeEndedAction) },
            ),
        )
    }

    Spacer(modifier = Modifier.height(Spacing.xs))

    EditorialSuffix(text = pageEditor.caption)

    Spacer(modifier = Modifier.height(Spacing.lg))

    ProgressBar(fraction = pageEditor.fraction)
}
