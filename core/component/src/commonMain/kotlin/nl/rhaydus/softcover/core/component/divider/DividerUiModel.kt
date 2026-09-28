package nl.rhaydus.softcover.core.component.divider

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/** Which hairline rule a [Divider] renders. */
@Immutable
data class DividerUiModel(val variant: DividerVariant) {
    companion object : UiModelPreviews<DividerUiModel> {
        override val previews: ImmutableList<DividerUiModel> = persistentListOf(
            DividerUiModel(DividerVariant.DebugRoutesRow),
            DividerUiModel(DividerVariant.ReadingLifeShareCard),
            DividerUiModel(DividerVariant.SettingsRoadmap),
            DividerUiModel(DividerVariant.CreateListQuote),
            DividerUiModel(DividerVariant.OnboardingOrTypeIt(label = "Or type it")),
        )
    }
}
