package nl.rhaydus.softcover.core.component.divider

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/** Which hairline rule a [Divider] renders. */
@Immutable
sealed interface DividerUiModel {
    data class Horizontal(
        val surface: HorizontalDividerSurface,
        val center: DividerCenter? = null,
    ) : DividerUiModel

    data class Vertical(val surface: VerticalDividerSurface) : DividerUiModel

    companion object : UiModelPreviews<DividerUiModel> {
        override val previews: ImmutableList<DividerUiModel> = persistentListOf(
            Horizontal(HorizontalDividerSurface.DebugRoutesRow),
            Horizontal(HorizontalDividerSurface.ReadingLifeShareCard),
            Horizontal(HorizontalDividerSurface.SettingsRoadmap),
            Horizontal(
                surface = HorizontalDividerSurface.CreateListQuote,
                center = DividerCenter.QuoteMark,
            ),
            Horizontal(
                surface = HorizontalDividerSurface.OnboardingOrTypeIt,
                center = DividerCenter.Label(text = "Or type it"),
            ),
            Vertical(VerticalDividerSurface.WidePaneSeparator),
        )
    }
}
