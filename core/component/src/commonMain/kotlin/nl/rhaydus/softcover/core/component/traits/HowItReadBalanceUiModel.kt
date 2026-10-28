package nl.rhaydus.softcover.core.component.traits

import androidx.compose.runtime.Immutable

/**
 * The slider group of [HowItReadSheet]. [value] runs 0f at the [startLabel] end to 1f at the [endLabel]
 * end, and null means unanswered: a bare track that the first touch sets. [clearLabel] shows only while
 * [value] is set; [unsetLabel] is what a screen reader says while it is not.
 */
@Immutable
data class HowItReadBalanceUiModel(
    val label: String,
    val value: Float?,
    val startLabel: String,
    val centreLabel: String,
    val endLabel: String,
    val clearLabel: String,
    val unsetLabel: String,
)
