package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/**
 * [SectionHeader] crossfaded between scopes. The lambda reads [modifier] so the compiler emits it as a
 * capturing instance; a capture-free one would be hoisted into a public `ComposableSingletons` class
 * whose signature leaks [SectionHeaderUiModel] into this module's ABI.
 */
@Composable
internal fun ScopedSectionHeader(
    header: SectionHeaderUiModel,
    modifier: Modifier = Modifier,
) {
    ScopeCrossfade(target = header) { scopedHeader ->
        SectionHeader(
            model = scopedHeader,
            modifier = modifier,
        )
    }
}
