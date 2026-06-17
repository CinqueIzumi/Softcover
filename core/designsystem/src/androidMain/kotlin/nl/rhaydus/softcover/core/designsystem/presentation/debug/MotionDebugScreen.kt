package nl.rhaydus.softcover.core.designsystem.presentation.debug

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
import androidx.compose.material3.Text
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.shakeOnError
import nl.rhaydus.softcover.core.designsystem.presentation.component.AnimatedStatNumber
import nl.rhaydus.softcover.core.designsystem.presentation.component.MarkAsReadBurst
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import kotlinx.coroutines.delay

object MotionDebugScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Screen(onNavigateBack = navigator::pop)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(onNavigateBack: () -> Unit) {
        Scaffold(
            topBar = {
                SoftcoverTopBar(
                    title = "Motion & haptics",
                    onNavigateBack = onNavigateBack,
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 24.dp,
                        vertical = 16.dp,
                    ),
            ) {
                EditorialSectionHeader(
                    eyebrow = "Debug",
                    headline = "Motion & haptics.",
                    description = "Fire individual animations and haptics in isolation. Useful for tuning timing and verifying reduced-motion behaviour.",
                )

                Spacer(modifier = Modifier.height(32.dp))

                HapticsSection()

                Spacer(modifier = Modifier.height(40.dp))

                MarkAsReadBurstSection()

                Spacer(modifier = Modifier.height(40.dp))

                ShakeOnErrorSection()

                Spacer(modifier = Modifier.height(40.dp))

                StatNumberSection()

                Spacer(modifier = Modifier.height(40.dp))

                WavyProgressSection()

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    @Composable
    private fun HapticsSection() {
        val haptics = rememberHaptics()

        SectionLabel(text = "Haptics")

        Spacer(modifier = Modifier.height(12.dp))

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
                .padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.padding(end = 12.dp)) {
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.editorialTypography.eyebrowSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
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
    private fun MarkAsReadBurstSection() {
        var triggerKey by remember { mutableIntStateOf(0) }

        SectionLabel(text = "Mark-as-read burst")

        Spacer(modifier = Modifier.height(12.dp))

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
    private fun ShakeOnErrorSection() {
        var trigger by remember { mutableStateOf(false) }

        SectionLabel(text = "Shake on error")

        Spacer(modifier = Modifier.height(12.dp))

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
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
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
    private fun StatNumberSection() {
        var value by remember { mutableIntStateOf(0) }

        SectionLabel(text = "Animated stat number")

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 20.dp,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedStatNumber(
                    value = value,
                    style = MaterialTheme.editorialTypography.statHero,
                    color = MaterialTheme.colorScheme.primary,
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
    private fun WavyProgressSection() {
        var progress by remember { mutableFloatStateOf(0.35f) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(2_000L)

                progress = (progress + 0.18f) % 1f
            }
        }

        SectionLabel(text = "Wavy progress")

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 20.dp,
                    ),
            ) {
                Text(
                    text = "Auto-advancing every 2s — confirms `LinearWavyProgressIndicator` tween smoothness.",
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(12.dp))

                LinearWavyProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    @Composable
    private fun SectionLabel(text: String) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
