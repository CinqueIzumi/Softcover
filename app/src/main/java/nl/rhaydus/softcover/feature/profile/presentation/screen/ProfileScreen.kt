package nl.rhaydus.softcover.feature.profile.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.core.presentation.component.AnimatedStatNumber
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverImage
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.modifier.shimmer
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.presentation.util.ObserveAsEvents
import nl.rhaydus.softcover.core.presentation.viewmodel.MainActivityViewModel
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.action.OnLogOutClickAction
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.event.LogOutUserEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileScreenScreenModel
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat

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
                    title = "",
                    onNavigateBack = onNavigateUp,
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                ProfileHeader(
                    profileImageUrl = state.userProfileData?.profileImageUrl,
                    name = state.userProfileData?.name.orEmpty(),
                    bio = state.userProfileData?.bio.orEmpty(),
                    isLoading = state.isLoading,
                )

                Spacer(modifier = Modifier.height(36.dp))

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    SectionLabel(text = "Reading atlas")

                    Spacer(modifier = Modifier.height(14.dp))

                    HeroStatCard(
                        eyebrow = "Total pages read",
                        value = state.userProfileData?.totalPagesRead ?: 0,
                        formatter = { integerFormat.format(it.toLong()) },
                        caption = "Every page a step further into the story.",
                        isLoading = state.isLoading,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        StatTile(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            eyebrow = "Volumes",
                            value = state.userProfileData?.booksRead ?: 0,
                            formatter = { integerFormat.format(it.toLong()) },
                            caption = "books read",
                            isLoading = state.isLoading,
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            SmallStatTile(
                                eyebrow = "Avg. rating",
                                value = (state.userProfileData?.averageRating ?: 0.0).toFloat(),
                                formatter = { ratingFormat.format(it.toDouble()) },
                                trailing = "★",
                                isLoading = state.isLoading,
                            )

                            SmallStatTile(
                                eyebrow = "Streak",
                                value = (state.userProfileData?.readingStreak ?: 0).toFloat(),
                                formatter = { it.toInt().toString() },
                                trailing = "days",
                                isLoading = state.isLoading,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                SoftcoverButton(
                    label = "Log out",
                    onClick = { runAction(OnLogOutClickAction()) },
                    style = ButtonStyle.TONAL,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 24.dp,
                            vertical = 24.dp,
                        ),
                    enabled = state.isLoading.not(),
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun ProfileHeader(
        profileImageUrl: String?,
        name: String,
        bio: String,
        isLoading: Boolean,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val shape = MaterialShapes.Cookie12Sided.toShape()

            SoftcoverImage(
                model = profileImageUrl,
                contentDescription = "User profile image",
                isLoading = isLoading,
                modifier = Modifier
                    .size(168.dp)
                    .clip(shape)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape,
                    ),
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel(text = "The reader")

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = name,
                style = MaterialTheme.editorialTypography.display,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .shimmer(isLoading = isLoading),
            )

            if (bio.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .width(28.dp)
                            .background(MaterialTheme.colorScheme.outline),
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )

                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .width(28.dp)
                            .background(MaterialTheme.colorScheme.outline),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "“$bio”",
                    style = MaterialTheme.editorialTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.shimmer(isLoading = isLoading),
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun HeroStatCard(
        eyebrow: String,
        value: Int,
        formatter: (Int) -> String,
        caption: String,
        isLoading: Boolean,
    ) {
        val shape = RoundedCornerShape(28.dp)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shimmer(shape = shape, isLoading = isLoading),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = shape,
        ) {
            val demotedContent = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            ) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.editorialTypography.eyebrow,
                    color = demotedContent,
                )

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedStatNumber(
                    value = value,
                    formatter = formatter,
                    style = MaterialTheme.editorialTypography.statHero,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 44.sp,
                        maxFontSize = 72.sp,
                        stepSize = 2.sp,
                    ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = caption,
                    style = MaterialTheme.editorialTypography.body,
                    color = demotedContent,
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun StatTile(
        eyebrow: String,
        value: Int,
        formatter: (Int) -> String,
        caption: String,
        isLoading: Boolean,
        modifier: Modifier = Modifier,
    ) {
        val shape = RoundedCornerShape(24.dp)

        Surface(
            modifier = modifier.shimmer(shape = shape, isLoading = isLoading),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = shape,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(20.dp))

                AnimatedStatNumber(
                    value = value,
                    formatter = formatter,
                    style = MaterialTheme.editorialTypography.statLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = caption,
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    @Composable
    private fun SmallStatTile(
        eyebrow: String,
        value: Float,
        formatter: (Float) -> String,
        trailing: String,
        isLoading: Boolean,
        modifier: Modifier = Modifier,
    ) {
        val shape = RoundedCornerShape(20.dp)

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .shimmer(shape = shape, isLoading = isLoading),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = shape,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    AnimatedStatNumber(
                        value = value,
                        formatter = formatter,
                        style = MaterialTheme.editorialTypography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = trailing,
                        style = MaterialTheme.editorialTypography.body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
        }
    }

    @Composable
    private fun SectionLabel(text: String) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .width(20.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = text.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrow,
                color = MaterialTheme.colorScheme.primary,
            )
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
                    totalPagesRead = 5_432,
                    averageRating = 4.2,
                    readingStreak = 7,
                ),
            ),
            runAction = {},
            onNavigateUp = {},
        )
    }
}
