internal data class ClassFileInfo(
    val name: String,
    val superName: String?,
    val interfaces: List<String>,
    val isConcrete: Boolean,
    val constructors: Set<String>,
)
