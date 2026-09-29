package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerUiModel

/**
 * A run of rows added through [content], each preceded by a hairline [Divider] — including the
 * first, so the rule sits directly under the group's section header. [selectableGroup] marks a
 * single-choice run (date notation, UI scale) for accessibility.
 */
@Composable
internal fun SettingsRowGroup(
    modifier: Modifier = Modifier,
    selectableGroup: Boolean = false,
    content: @Composable SettingsRowGroupScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (selectableGroup) Modifier.selectableGroup() else Modifier),
    ) {
        val scope = remember {
            object : SettingsRowGroupScope {
                @Composable
                override fun row(content: @Composable () -> Unit) {
                    Divider(model = DividerUiModel.Horizontal())
                    content()
                }
            }
        }
        scope.content()
    }
}

/**
 * Receiver [SettingsRowGroup] passes to its `content` block, so a row's divider is drawn by
 * [row] rather than left to each call site to repeat.
 */
internal interface SettingsRowGroupScope {
    @Composable
    fun row(content: @Composable () -> Unit)
}
