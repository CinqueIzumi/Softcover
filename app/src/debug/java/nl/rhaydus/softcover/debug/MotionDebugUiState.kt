package nl.rhaydus.softcover.debug

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.toad.UiState

data class MotionDebugUiState(
    val hapticsHeader: SectionHeaderUiModel.Label = SectionHeaderUiModel.Label(eyebrow = "Haptics"),
    val markAsReadBurstHeader: SectionHeaderUiModel.Label = SectionHeaderUiModel.Label(
        eyebrow = "Mark-as-read burst",
    ),
    val shakeOnErrorHeader: SectionHeaderUiModel.Label = SectionHeaderUiModel.Label(eyebrow = "Shake on error"),
    val statNumberHeader: SectionHeaderUiModel.Label = SectionHeaderUiModel.Label(
        eyebrow = "Animated stat number",
    ),
    val wavyProgressHeader: SectionHeaderUiModel.Label = SectionHeaderUiModel.Label(eyebrow = "Wavy progress"),
) : UiState
