package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
            .map { it.readingLife to it.userProfileData }
            .distinctUntilChanged()
            .collectLatest { (readingLife, profile) ->
                val card = if (readingLife != null && profile != null) {
                    readingLife.toReadingLifeShareCardUiModel(profile)
                } else {
                    null
                }

                scope.setState {
                    it.copy(
                        shareSheet = card?.let { shareCard ->
                            profileShareSheetFor(
                                card = shareCard,
                                progress = it.shareCapture.progress,
                                closeRequested = it.shareCapture.closeRequested,
                            )
                        },
                    )
                }
            }
    }
}
