package nl.rhaydus.softcover.core.designsystem.presentation.model

class SoftcoverMenuItem(
    val label: String,
    val onClick: () -> Unit,
    val icon: SoftcoverIconResource,
)
