package nl.rhaydus.softcover.apollo

import com.apollographql.apollo.ast.GQLDocument
import com.apollographql.apollo.ast.GQLFieldDefinition
import com.apollographql.apollo.ast.GQLListType
import com.apollographql.apollo.ast.GQLNamedType
import com.apollographql.apollo.ast.GQLObjectTypeDefinition
import com.apollographql.apollo.ast.parseAsGQLDocument
import com.apollographql.apollo.compiler.ApolloCompiler
import com.apollographql.apollo.compiler.ApolloCompilerPluginEnvironment
import com.apollographql.apollo.compiler.ApolloCompilerRegistry
import com.apollographql.apollo.compiler.Order
import com.apollographql.apollo.compiler.SchemaDocumentTransform
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SchemaCorrectionsPluginProviderTest {
    private val environment = ApolloCompilerPluginEnvironment(
        arguments = emptyMap(),
        logger = mockk<ApolloCompiler.Logger>(relaxed = true),
    )

    private fun registeredTransform(): SchemaDocumentTransform {
        val registry = mockk<ApolloCompilerRegistry>(relaxed = true)
        val transform = slot<SchemaDocumentTransform>()
        SchemaCorrectionsPluginProvider()
            .create(environment)
            .beforeCompilationStep(
                environment,
                registry,
            )
        verify {
            registry.registerSchemaTransform(
                any(),
                *anyVararg<Order>(),
                transform = capture(transform),
            )
        }
        return transform.captured
    }

    private fun GQLDocument.objectType(name: String): GQLObjectTypeDefinition =
        definitions.filterIsInstance<GQLObjectTypeDefinition>().first { it.name == name }

    private fun GQLObjectTypeDefinition.field(name: String): GQLFieldDefinition =
        fields.first { it.name == name }

    private fun parse(sdl: String): GQLDocument = sdl.parseAsGQLDocument().getOrThrow()

    @Nested
    inner class ListIdTypeErrors {
        @Test
        fun `rewrites ListIdType errors from a list of String to the named type String`() {
            // ----- Arrange -----
            val transform = registeredTransform()
            val document = parse("type ListIdType { id: Int errors: [String] }")

            // ----- Act -----
            val corrected = transform.transform(document)

            // ----- Assert -----
            val type = corrected.objectType("ListIdType").field("errors").type
            type.shouldBeInstanceOf<GQLNamedType>().name shouldBe "String"
        }

        @Test
        fun `leaves the other ListIdType fields untouched`() {
            // ----- Arrange -----
            val transform = registeredTransform()
            val document = parse("type ListIdType { id: Int errors: [String] list_id: Int }")

            // ----- Act -----
            val corrected = transform.transform(document)

            // ----- Assert -----
            val fields = corrected.objectType("ListIdType").fields
            fields shouldHaveSize 3
            fields.first { it.name == "id" } shouldBe document.objectType("ListIdType").field("id")
            fields.first { it.name == "list_id" } shouldBe document.objectType("ListIdType").field("list_id")
        }
    }

    @Nested
    inner class OtherTypes {
        @Test
        fun `leaves a same-named errors field on another type untouched`() {
            // ----- Arrange -----
            val transform = registeredTransform()
            val document = parse("type NewBookIdType { errors: [String] } type ListIdType { errors: [String] }")

            // ----- Act -----
            val corrected = transform.transform(document)

            // ----- Assert -----
            val untouched = corrected.objectType("NewBookIdType").field("errors")
            untouched shouldBe document.objectType("NewBookIdType").field("errors")
            untouched.type.shouldBeInstanceOf<GQLListType>()
        }

        @Test
        fun `keeps every definition in the document`() {
            // ----- Arrange -----
            val transform = registeredTransform()
            val document = parse("type Query { a: Int } scalar Json type ListIdType { errors: [String] }")

            // ----- Act -----
            val corrected = transform.transform(document)

            // ----- Assert -----
            corrected.definitions.map { it::class } shouldBe document.definitions.map { it::class }
            corrected.objectType("Query").shouldNotBeNull() shouldBe document.objectType("Query")
        }
    }
}
