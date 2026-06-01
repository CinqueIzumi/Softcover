package nl.rhaydus.softcover.core.presentation.model

class SoftcoverMenuItem(
    val label: String,
    val onClick: () -> Unit,
    val icon: SoftcoverIconResource,
)