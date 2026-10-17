import java.io.DataInputStream
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

internal object ClassFileReader {
    private const val MAGIC = 0xCAFEBABE.toInt()
    private const val ACC_INTERFACE = 0x0200
    private const val ACC_ABSTRACT = 0x0400
    private const val ACC_MODULE = 0x8000
    private const val CONSTRUCTOR = "<init>"

    fun readAll(
        jars: List<File>,
        directories: List<File>,
    ): Map<String, ClassFileInfo> {
        val classes = HashMap<String, ClassFileInfo>()

        jars.filter { it.isFile }.forEach { jar ->
            ZipFile(jar).use { zip ->
                zip.entries().asSequence()
                    .filter { it.isDirectory.not() && it.name.isClassEntry() }
                    .forEach { entry ->
                        zip.getInputStream(entry).use { stream -> read(stream)?.let { classes[it.name] = it } }
                    }
            }
        }

        directories.filter { it.isDirectory }.forEach { directory ->
            directory.walkTopDown()
                .filter { it.isFile && it.name.endsWith(".class") }
                .forEach { file ->
                    file.inputStream().use { stream -> read(stream)?.let { classes[it.name] = it } }
                }
        }

        return classes
    }

    private fun String.isClassEntry(): Boolean {
        return endsWith(".class") && startsWith("META-INF/").not() && endsWith("module-info.class").not()
    }

    private fun read(stream: InputStream): ClassFileInfo? {
        val input = DataInputStream(stream.buffered())

        if (input.readInt() != MAGIC) return null

        input.skipBytes(Short.SIZE_BYTES * 2)

        val pool = readConstantPool(input)
        val access = input.readUnsignedShort()

        if (access and ACC_MODULE != 0) return null

        val name = pool.className(input.readUnsignedShort()) ?: return null

        val superName = pool.className(input.readUnsignedShort())
        val interfaces = List(input.readUnsignedShort()) { pool.className(input.readUnsignedShort()) }

        repeat(input.readUnsignedShort()) { skipMember(input) }

        val constructors = HashSet<String>()

        repeat(input.readUnsignedShort()) {
            input.skipBytes(Short.SIZE_BYTES)

            val memberName = pool.utf8(input.readUnsignedShort())
            val descriptor = pool.utf8(input.readUnsignedShort())

            if (memberName == CONSTRUCTOR && descriptor != null) constructors += descriptor

            skipAttributes(input)
        }

        return ClassFileInfo(
            name = name,
            superName = superName,
            interfaces = interfaces.filterNotNull(),
            isConcrete = access and (ACC_INTERFACE or ACC_ABSTRACT) == 0,
            constructors = constructors,
        )
    }

    private fun skipMember(input: DataInputStream) {
        input.skipBytes(Short.SIZE_BYTES * 3)
        skipAttributes(input)
    }

    private fun skipAttributes(input: DataInputStream) {
        repeat(input.readUnsignedShort()) {
            input.skipBytes(Short.SIZE_BYTES)
            input.skipBytes(input.readInt())
        }
    }

    private fun readConstantPool(input: DataInputStream): ConstantPool {
        val count = input.readUnsignedShort()
        val utf8 = arrayOfNulls<String>(count)
        val classNameIndex = IntArray(count)
        var index = 1

        while (index < count) {
            when (input.readUnsignedByte()) {
                1 -> utf8[index] = input.readUTF()
                7 -> classNameIndex[index] = input.readUnsignedShort()
                3, 4, 9, 10, 11, 12, 17, 18 -> input.skipBytes(Int.SIZE_BYTES)
                5, 6 -> {
                    input.skipBytes(Long.SIZE_BYTES)
                    index++
                }
                8, 16, 19, 20 -> input.skipBytes(Short.SIZE_BYTES)
                15 -> input.skipBytes(Short.SIZE_BYTES + Byte.SIZE_BYTES)
                else -> error("Unknown constant pool tag")
            }

            index++
        }

        return ConstantPool(
            utf8,
            classNameIndex,
        )
    }

    private class ConstantPool(
        private val utf8: Array<String?>,
        private val classNameIndex: IntArray,
    ) {
        fun utf8(index: Int): String? = utf8.getOrNull(index)

        fun className(index: Int): String? {
            if (index == 0) return null

            return utf8(classNameIndex[index])?.replace(
                '/',
                '.',
            )
        }
    }
}
