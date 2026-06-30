package nl.rhaydus.softcover.core.designsystem.presentation.model

import nl.rhaydus.softcover.core.domain.model.ProgressUnit

fun ProgressSheetTab.toProgressUnit(): ProgressUnit = when (this) {
    ProgressSheetTab.PAGE -> ProgressUnit.PAGE
    ProgressSheetTab.TIME -> ProgressUnit.TIME
    ProgressSheetTab.PERCENTAGE -> ProgressUnit.PERCENTAGE
}

fun ProgressUnit.toProgressSheetTab(): ProgressSheetTab = when (this) {
    ProgressUnit.PAGE -> ProgressSheetTab.PAGE
    ProgressUnit.TIME -> ProgressSheetTab.TIME
    ProgressUnit.PERCENTAGE -> ProgressSheetTab.PERCENTAGE
}
