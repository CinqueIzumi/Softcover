package nl.rhaydus.softcover.feature.explore.data.datastore.serializer

import kotlinx.serialization.Serializable

@Serializable
internal data class SearchHistoryEntity(
    val previousQueries: List<String> = emptyList(),
)
