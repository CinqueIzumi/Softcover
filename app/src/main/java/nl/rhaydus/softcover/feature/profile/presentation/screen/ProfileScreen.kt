package nl.rhaydus.softcover.feature.profile.presentation.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
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
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.action.OnLogOutClickAction
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.event.LogOutUserEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileScreenScreenModel
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
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
            onNavigateUp = navigator::pop,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun Screen(
        state: ProfileUiState,
        runAction: (ProfileAction) -> Unit,
        onNavigateUp: () -> Unit,
    ) {
        val locale = LocalConfiguration.current.locales[0]
        val integerFormat = remember(locale) { NumberFormat.getIntegerInstance(locale) }
        val ratingFormat = remember(locale) {
            NumberFormat.getNumberInstance(locale).apply {
                minimumFractionDigits = 1
                maximumFractionDigits = 1
            }
        }

        Scaffold(
            topBar = {
                SoftcoverTopBar(
                    title = "Profile",
                    onNavigateBack = onNavigateUp,
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
                    val shape = MaterialShapes.Cookie12Sided.toShape()

                    SoftcoverImage(
                        model = state.userProfileData?.profileImageUrl,
                        contentDescription = "User profile image",
                        isLoading = state.isLoading,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(shape)
                            .border(
                                width = 4.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = shape
                            )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = state.userProfileData?.name ?: "",
                        style = MaterialTheme.typography.displaySmallEmphasized.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontStyle = FontStyle.Italic
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shimmer(isLoading = state.isLoading),
                    )

                    state.userProfileData?.bio?.takeIf { it != "" }?.let { bio ->
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = bio ?: "",
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .shimmer(isLoading = state.isLoading)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val totalPagesShape = RoundedCornerShape(32.dp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shimmer(shape = totalPagesShape, isLoading = state.isLoading),
                        tonalElevation = 1.dp,
                        shape = totalPagesShape,
                    ) {
                        Column(
                            modifier = Modifier.padding(all = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "TOTAL PAGES READ",
                                style = MaterialTheme.typography.labelSmallEmphasized.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            Text(
                                text = integerFormat.format(state.userProfileData?.totalPagesRead ?: 0),
                                style = MaterialTheme.typography.displayLargeEmphasized.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontStyle = FontStyle.Italic
                                ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max)
                    ) {
                        StatsBox(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            isLoading = state.isLoading,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(all = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = "BOOKS READ",
                                    style = MaterialTheme.typography.labelSmallEmphasized.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = integerFormat.format(state.userProfileData?.booksRead ?: 0),
                                    style = MaterialTheme.typography.displaySmallEmphasized.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                    ),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            StatsBox(
                                modifier = Modifier.fillMaxWidth(),
                                isLoading = state.isLoading,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(all = 8.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = "AVG. RATING",
                                        style = MaterialTheme.typography.labelSmallEmphasized.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )

                                    Text(
                                        text = ratingFormat.format(state.userProfileData?.averageRating ?: 0.0),
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            StatsBox(
                                modifier = Modifier.fillMaxWidth(),
                                isLoading = state.isLoading,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "STREAK (DAYS)",
                                        style = MaterialTheme.typography.labelSmallEmphasized.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )

                                    Text(
                                        text = "${state.userProfileData?.readingStreak ?: 0}",
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                }
                            }
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
    private fun StatsBox(
        modifier: Modifier = Modifier,
        tonalElevation: Dp = 1.dp,
        isLoading: Boolean = false,
        content: @Composable () -> Unit,
    ) {
        val shape = RoundedCornerShape(16.dp)
        Surface(
            modifier = modifier.shimmer(shape = shape, isLoading = isLoading),
            tonalElevation = tonalElevation,
            content = content,
            shape = shape,
        )
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
                    bio = "",
//                    bio = "Lover of classic literature and sci-fi.",
                    booksRead = 20,
                    totalPagesRead = 5_432,
                    averageRating = 4.2,
                    readingStreak = 7,
                )
            ),
            runAction = {},
            onNavigateUp = {},
        )
    }
}