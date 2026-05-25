package nl.rhaydus.softcover.feature.lists.domain.exception

class ListNameTakenException(
    val name: String,
) : Exception("List name \"$name\" is already taken")
