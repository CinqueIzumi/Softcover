package nl.rhaydus.softcover.feature.reading.presentation.state

import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress

data class ReadingScreenUiState(
    val books: List<BookWithProgress> = emptyList(),
    val isLoading: Boolean = true,
    val bookToUpdate: BookWithProgress? = null,
    val progressTab: ProgressTab = ProgressTab.PAGE,
)

enum class ProgressTab(val tabName: String) {
    PAGE(tabName = "Page"),
    PERCENTAGE(tabName = "Percentage")
}