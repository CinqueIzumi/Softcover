import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ClassFileReaderTest {
    @TempDir
    lateinit var directory: File

    private fun copyClass(type: Class<*>) {
        val path = type.name.replace(
            '.',
            '/',
        ) + ".class"
        val bytes = type.classLoader.getResourceAsStream(path).shouldNotBeNull().use { it.readBytes() }

        directory.resolve(path).apply {
            parentFile.mkdirs()
            writeBytes(bytes)
        }
    }

    @Nested
    inner class ReadAll {
        @Test
        fun `reads name, supertype, interfaces and constructors past long and double constants`() {
            // ----- Arrange -----
            copyClass(ReaderFixture::class.java)

            // ----- Act -----
            val info = ClassFileReader.readAll(
                emptyList(),
                listOf(directory),
            )["ReaderFixture"]

            // ----- Assert -----
            info.shouldNotBeNull()
            info.superName shouldBe "java.lang.Object"
            info.interfaces shouldContainExactly listOf("java.lang.Runnable")
            info.isConcrete shouldBe true
            info.constructors shouldBe setOf(
                "()V",
                "(Ljava/lang/String;)V",
            )
        }

        @Test
        fun `marks abstract classes and interfaces as not concrete`() {
            // ----- Arrange -----
            copyClass(ReaderAbstractFixture::class.java)
            copyClass(ReaderInterfaceFixture::class.java)

            // ----- Act -----
            val classes = ClassFileReader.readAll(
                emptyList(),
                listOf(directory),
            )

            // ----- Assert -----
            classes["ReaderAbstractFixture"].shouldNotBeNull().isConcrete shouldBe false
            classes["ReaderInterfaceFixture"].shouldNotBeNull().isConcrete shouldBe false
        }

        @Test
        fun `ignores files that are not class files and missing locations`() {
            // ----- Arrange -----
            directory.resolve("notes.class").writeText("not bytecode at all")
            directory.resolve("readme.txt").writeText("text")

            // ----- Act -----
            val classes = ClassFileReader.readAll(
                listOf(directory.resolve("missing.jar")),
                listOf(
                    directory,
                    directory.resolve("missing"),
                ),
            )

            // ----- Assert -----
            classes.isEmpty() shouldBe true
        }

        @Test
        fun `reads classes from a jar`() {
            // ----- Arrange -----
            copyClass(ReaderFixture::class.java)
            val jar = File(
                directory,
                "fixture.jar",
            )
            ZipOutputStream(jar.outputStream()).use { zip ->
                zip.putNextEntry(ZipEntry("ReaderFixture.class"))
                zip.write(directory.resolve("ReaderFixture.class").readBytes())
                zip.closeEntry()
            }

            // ----- Act -----
            val classes = ClassFileReader.readAll(
                listOf(jar),
                emptyList(),
            )

            // ----- Assert -----
            classes shouldContainKey "ReaderFixture"
        }
    }
}
