package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toReadingLifeShareCardUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.profileShareSheetFor
import nl.rhaydus.toad.ActionScope

internal class ReadingLifeShareCardCollector : ProfileCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
        dependencies: ProfileDependencies,
    ) {
        scope.state
            .map { state ->
                val readingLife = state.readingLife
                val profile = state.userProfileData

                ShareSheetSnapshot(
                    card = if (state.isShareSheetRequested && readingLife != null && profile != null) {
                        readingLife.toReadingLifeShareCardUiModel(profile)
                    } else {
                        null
                    },
                    progress = state.shareCapture.progress,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val card = snapshot.card

                scope.setState {
                    it.copy(
                        shareSheet = if (card == null || it.isShareSheetRequested.not()) {
                            it.shareSheet.requestClose()
                        } else {
                            it.shareSheet.openOrUpdate(
                                model = profileShareSheetFor(
                                    card = card,
                                    progress = snapshot.progress,
                                ),
                            )
                        },
                    )
                }
            }
    }
}
