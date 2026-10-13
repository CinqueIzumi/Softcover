package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

internal fun navFadeThrough(
    effectsSpec: FiniteAnimationSpec<Float>,
    enterMotion: EnterTransition = EnterTransition.None,
    exitMotion: ExitTransition = ExitTransition.None,
): ContentTransform = (fadeIn(animationSpec = effectsSpec) + enterMotion) togetherWith
    (fadeOut(animationSpec = effectsSpec) + exitMotion)
