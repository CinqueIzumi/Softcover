package nl.rhaydus.softcover.core.designsystem.preset

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class VectorToSvgTest {
    @TempDir
    lateinit var dir: File

    private fun convert(
        body: String,
        monochrome: Boolean = false,
    ): String {
        val source = dir.resolve("icon.xml")
        source.writeText(
            """
            <vector xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:aapt="http://schemas.android.com/aapt"
                android:width="24dp" android:height="24dp"
                android:viewportWidth="48" android:viewportHeight="40">
            $body
            </vector>
            """.trimIndent(),
        )

        return VectorToSvg(
            monochrome = monochrome,
            colorDir = dir,
        ).convert(source)
    }

    @Nested
    inner class Convert {
        @Test
        fun `takes the viewBox from the viewport and the size from width and height`() {
            // ----- Arrange -----
            val path = """<path android:pathData="M0,0L1,1" android:fillColor="#112233"/>"""

            // ----- Act -----
            val result = convert(path)

            // ----- Assert -----
            result shouldContain """width="24" height="24""""
            result shouldContain """viewBox="0 0 48 40""""
            result shouldContain """<path d="M0,0L1,1" fill="#112233"/>"""
        }

        @Test
        fun `maps evenOdd fill type to fill-rule`() {
            // ----- Arrange -----
            val path = """<path android:pathData="M0,0" android:fillColor="#112233" android:fillType="evenOdd"/>"""

            // ----- Act -----
            val result = convert(path)

            // ----- Assert -----
            result shouldContain """fill-rule="evenodd""""
        }

        @Test
        fun `omits fill-rule for the default fill type`() {
            // ----- Arrange -----
            val path = """<path android:pathData="M0,0" android:fillColor="#112233"/>"""

            // ----- Act -----
            val result = convert(path)

            // ----- Assert -----
            result shouldNotContain "fill-rule"
        }

        @Test
        fun `wraps siblings after a clip-path in a clipped group`() {
            // ----- Arrange -----
            val body = """
                <clip-path android:pathData="M0,0L5,5"/>
                <path android:pathData="M1,1" android:fillColor="#112233"/>
            """.trimIndent()

            // ----- Act -----
            val result = convert(body)

            // ----- Assert -----
            result shouldContain """<clipPath id="clip-0"><path d="M0,0L5,5"/></clipPath>"""
            result shouldContain """<g clip-path="url(#clip-0)">"""
        }

        @Test
        fun `emits a linear gradient as a definition referenced by the fill`() {
            // ----- Arrange -----
            val body = """
                <path android:pathData="M0,0">
                    <aapt:attr name="android:fillColor">
                        <gradient android:type="linear" android:startX="0" android:startY="0"
                            android:endX="10" android:endY="20">
                            <item android:offset="0" android:color="#FF0000"/>
                            <item android:offset="1" android:color="#0000FF"/>
                        </gradient>
                    </aapt:attr>
                </path>
            """.trimIndent()

            // ----- Act -----
            val result = convert(body)

            // ----- Assert -----
            result shouldContain """<linearGradient id="gradient-0""""
            result shouldContain """x2="10" y2="20""""
            result shouldContain """stop-color="#0000ff""""
            result shouldContain """fill="url(#gradient-0)""""
        }

        @Test
        fun `converts group rotation and pivot into a transform`() {
            // ----- Arrange -----
            val body = """
                <group android:rotation="90" android:pivotX="4" android:pivotY="6">
                    <path android:pathData="M0,0" android:fillColor="#112233"/>
                </group>
            """.trimIndent()

            // ----- Act -----
            val result = convert(body)

            // ----- Assert -----
            result shouldContain """<g transform="translate(4 6) rotate(90) scale(1 1) translate(-4 -6)">"""
        }

        @Test
        fun `emits a plain group when it carries no transform attributes`() {
            // ----- Arrange -----
            val body = """<group><path android:pathData="M0,0" android:fillColor="#112233"/></group>"""

            // ----- Act -----
            val result = convert(body)

            // ----- Assert -----
            result.lines().contains("<g>") shouldBe true
        }

        @Test
        fun `tints every visible colour as currentColor when monochrome`() {
            // ----- Arrange -----
            val path = """<path android:pathData="M0,0" android:fillColor="#112233"/>"""

            // ----- Act -----
            val result = convert(
                path,
                monochrome = true,
            )

            // ----- Assert -----
            result shouldContain """fill="currentColor""""
        }

        @Test
        fun `fails on an unknown element`() {
            // ----- Arrange -----
            val body = """<mystery/>"""

            // ----- Act & Assert -----
            val error = shouldThrow<IllegalStateException> { convert(body) }
            error.message shouldContain "mystery"
        }

        @Test
        fun `fails on an unsupported attribute`() {
            // ----- Arrange -----
            val body = """<path android:pathData="M0,0" android:trimPathEnd="0.5"/>"""

            // ----- Act & Assert -----
            val error = shouldThrow<IllegalStateException> { convert(body) }
            error.message shouldContain "trimPathEnd"
        }
    }
}
