package nl.rhaydus.softcover.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.shakeOnError
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurst
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/** Fixed for this screen, so the model is a constant rather than rebuilt per frame. */
private val MOTION_DEBUG_TOP_BAR = TopBarUiModel(
    title = "Motion & haptics",
)

object MotionDebugScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<MotionDebugScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        Screen(
            state = state,
            onNavigateBack = navigator::pop,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        state: MotionDebugUiState,
        onNavigateBack: () -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    model = MOTION_DEBUG_TOP_BAR,
                    onEvent = { event ->
                        when (event) {
                            TopBarEvent.BackClicked -> onNavigateBack()
                        }
                    },
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = Spacing.gutter,
                        vertical = Spacing.md,
                    ),
            ) {
                SectionHeader(model = state.pageHeader)

                Spacer(modifier = Modifier.height(Spacing.xxxl))

                HapticsSection(header = state.hapticsHeader)

                Spacer(modifier = Modifier.height(Spacing.section))

                MarkAsReadBurstSection(header = state.markAsReadBurstHeader)

                Spacer(modifier = Modifier.height(Spacing.section))

                ShakeOnErrorSection(header = state.shakeOnErrorHeader)

                Spacer(modifier = Modifier.height(Spacing.section))

                StatNumberSection(header = state.statNumberHeader)

                Spacer(modifier = Modifier.height(Spacing.section))

                WavyProgressSection(header = state.wavyProgressHeader)

                Spacer(modifier = Modifier.height(Spacing.x5l))
            }
        }
    }

    @Composable
    private fun HapticsSection(header: SectionHeaderUiModel.Label) {
        val haptics = rememberHaptics()

        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(Spacing.md))

        Column(modifier = Modifier.fillMaxWidth()) {
            HapticRow(label = "commit", description = "Successful save / mark-as-read") { haptics.commit() }

            HapticRow(label = "reject", description = "Mutation rolled back") { haptics.reject() }

            HapticRow(label = "select", description = "Neutral chip / segment toggle") { haptics.select() }

            HapticRow(label = "threshold", description = "Pull-to-refresh trigger, peek activation") { haptics.threshold() }

            HapticRow(label = "tickle", description = "Per-integer pass on a slider") { haptics.tickle() }

            HapticRow(label = "lift", description = "Drag-to-reorder pickup") { haptics.lift() }

            HapticRow(label = "drop", description = "Drag-to-reorder settle") { haptics.drop() }

            HapticRow(label = "milestone", description = "Year-end goal, streak, series completion") { haptics.milestone() }
        }
    }

    @Composable
    private fun HapticRow(
        label: String,
        description: String,
        onFire: () -> Unit,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.xxs),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Spacing.md,
                        vertical = Spacing.sm,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.padding(end = Spacing.sm)) {
                    SoftcoverText(
                        text = label.uppercase(),
                        style = MaterialTheme.editorialTypography.eyebrowSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(modifier = Modifier.height(Spacing.xxxs))

                    SoftcoverText(
                        text = description,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                RhaydusButton(
                    label = "Fire",
                    style = ButtonStyle.TONAL,
                    size = ButtonSize.XS,
                    onClick = onFire,
                )
            }
        }
    }

    @Composable
    private fun MarkAsReadBurstSection(header: SectionHeaderUiModel.Label) {
        var triggerKey by remember { mutableIntStateOf(0) }

        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(Spacing.md))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {}

            MarkAsReadBurst(
                triggerKey = triggerKey,
                modifier = Modifier.size(160.dp),
            )

            RhaydusButton(
                label = "Fire burst",
                style = ButtonStyle.TONAL,
                size = ButtonSize.S,
                onClick = { triggerKey += 1 },
            )
        }
    }

    @Composable
    private fun ShakeOnErrorSection(header: SectionHeaderUiModel.Label) {
        var trigger by remember { mutableStateOf(false) }

        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(Spacing.md))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .shakeOnError(
                    trigger = trigger,
                    onShakeEnd = { trigger = false },
                ),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SoftcoverText(
                    text = "Tap to shake",
                    style = MaterialTheme.editorialTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                RhaydusButton(
                    label = "Fire shake",
                    style = ButtonStyle.TONAL,
                    size = ButtonSize.XS,
                    onClick = { trigger = true },
                )
            }
        }
    }

    @Composable
    private fun StatNumberSection(header: SectionHeaderUiModel.Label) {
        var value by remember { mutableIntStateOf(0) }

        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(Spacing.md))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Spacing.lg,
                        vertical = Spacing.lg,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatNumber(
                    model = remember(value) { StatNumberUiModel(value = value.toDouble()) },
                    style = MaterialTheme.editorialTypography.statHero,
                )

                RhaydusButton(
                    label = "+ Random",
                    style = ButtonStyle.TONAL,
                    size = ButtonSize.XS,
                    onClick = { value = (value + (50..500).random()) },
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun WavyProgressSection(header: SectionHeaderUiModel.Label) {
        var progress by remember { mutableFloatStateOf(0.35f) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(2_000L)

                progress = (progress + 0.18f) % 1f
            }
        }

        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(Spacing.md))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Spacing.lg,
                        vertical = Spacing.lg,
                    ),
            ) {
                SoftcoverText(
                    text = "Auto-advancing every 2s — confirms `LinearWavyProgressIndicator` tween smoothness.",
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                LinearWavyProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
