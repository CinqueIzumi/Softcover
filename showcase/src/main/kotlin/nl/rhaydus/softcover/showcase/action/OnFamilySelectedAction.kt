package nl.rhaydus.softcover.showcase.action

import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.showcase.event.ShowcaseEvent
import nl.rhaydus.softcover.showcase.mapper.showcaseFamilyChips
import nl.rhaydus.softcover.showcase.screenmodel.ShowcaseDependencies
import nl.rhaydus.softcover.showcase.state.ShowcaseLocalVariables
import nl.rhaydus.softcover.showcase.state.ShowcaseUiState
import nl.rhaydus.toad.ActionScope

internal class OnFamilySelectedAction(val family: GalleryFamily) : ShowcaseAction {
    override suspend fun execute(
        dependencies: ShowcaseDependencies,
        scope: ActionScope<ShowcaseUiState, ShowcaseEvent, ShowcaseLocalVariables>,
    ) {
        scope.setState {
            val nextFamily = if (it.selectedFamily == family) null else family

            it.copy(
                selectedFamily = nextFamily,
                familyChips = showcaseFamilyChips(selected = nextFamily),
            )
        }
    }
}
