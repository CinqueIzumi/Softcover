package nl.rhaydus.softcover.feature.profile.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverImage
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.modifier.shimmer
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.presentation.util.ObserveAsEvents
import nl.rhaydus.softcover.core.presentation.viewmodel.MainActivityViewModel
import nl.rhaydus.softcover.feature.profile.presentation.action.OnLogOutClickAction
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.event.LogOutUserEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileScreenScreenModel
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.settings.domain.model.UserProfileData
import org.koin.androidx.compose.koinViewModel

class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ProfileScreenScreenModel>()
        val mainVm = koinViewModel<MainActivityViewModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val navigator = LocalNavigator.currentOrThrow

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                is LogOutUserEvent -> mainVm.setUserAuthenticated(authenticated = false)
            }
        }

        Screen(
            state = state,
            runAction = screenModel::runAction,
            onNavigateUp = navigator::pop
        )
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun Screen(
        state: ProfileUiState,
        runAction: (ProfileAction) -> Unit,
        onNavigateUp: () -> Unit,
    ) {
        Scaffold(
            topBar = {
                SoftcoverTopBar(
                    title = "Profile",
                    onNavigateBack = onNavigateUp
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SoftcoverImage(
                        url = state.userProfileData?.profileImageUrl,
                        contentDescription = "User profile image",
                        isLoading = state.isLoading,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(shape = MaterialShapes.Cookie12Sided.toShape())
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = state.userProfileData?.name ?: "",
                        style = MaterialTheme.typography.headlineLargeEmphasized,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shimmer(isLoading = state.isLoading),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = state.userProfileData?.bio ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shimmer(isLoading = state.isLoading)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shimmer(isLoading = state.isLoading)
                    ) {
                        StatsBox(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Books read",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${state.userProfileData?.booksRead ?: -1}",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                SoftcoverButton(
                    label = "Log out",
                    onClick = { runAction(OnLogOutClickAction()) },
                    style = ButtonStyle.TONAL,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 32.dp,
                        ),
                    enabled = state.isLoading.not(),
                )
            }
        }
    }

    @Composable
    fun StatsBox(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    ) {
        Surface(
            tonalElevation = 2.dp,
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(all = 16.dp),
            ) {
                content()
            }
        }
    }
}

@StandardPreview
@Composable
private fun ProfileScreenPreview() {
    SoftcoverTheme {
        ProfileScreen().Screen(
            state = ProfileUiState(
                isLoading = false,
                userProfileData = UserProfileData(
                    profileImageUrl = "",
                    name = "Cinque",
                    bio = "Lover of classic literature and sci-fi.",
                    booksRead = 20,
                )
            ),
            runAction = {},
            onNavigateUp = {},
        )
    }
}