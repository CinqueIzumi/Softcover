package nl.rhaydus.softcover.core.lists.domain.exception

class ListNameTakenException(
    val name: String,
) : Exception("List name \"$name\" is already taken")
