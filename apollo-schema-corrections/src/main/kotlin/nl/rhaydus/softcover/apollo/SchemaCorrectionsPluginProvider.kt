package nl.rhaydus.softcover.apollo

import com.apollographql.apollo.ast.GQLDocument
import com.apollographql.apollo.ast.GQLNamedType
import com.apollographql.apollo.ast.GQLObjectTypeDefinition
import com.apollographql.apollo.compiler.ApolloCompilerPlugin
import com.apollographql.apollo.compiler.ApolloCompilerPluginEnvironment
import com.apollographql.apollo.compiler.ApolloCompilerPluginProvider
import com.apollographql.apollo.compiler.ApolloCompilerRegistry
import com.apollographql.apollo.compiler.SchemaDocumentTransform

// The server contradicts its own introspection for these fields, so the schema is rewritten to what is served.
private val corrections = listOf(
    FieldCorrection(
        typeName = "ListIdType",
        fieldName = "errors",
        servedType = "String",
    ),
)

class SchemaCorrectionsPluginProvider : ApolloCompilerPluginProvider {
    override fun create(environment: ApolloCompilerPluginEnvironment): ApolloCompilerPlugin =
        object : ApolloCompilerPlugin {
            override fun beforeCompilationStep(
                environment: ApolloCompilerPluginEnvironment,
                registry: ApolloCompilerRegistry,
            ) {
                registry.registerSchemaTransform(
                    "schema-corrections",
                    transform = SchemaDocumentTransform { document -> document.corrected() },
                )
            }
        }
}

private fun GQLDocument.corrected(): GQLDocument =
    copy(
        definitions = definitions.map { definition ->
            if (definition is GQLObjectTypeDefinition) definition.withCorrectedFields() else definition
        },
    )

private fun GQLObjectTypeDefinition.withCorrectedFields(): GQLObjectTypeDefinition {
    val typeCorrections = corrections.filter { it.typeName == name }
    if (typeCorrections.isEmpty()) return this

    return copy(
        fields = fields.map { field ->
            val correction = typeCorrections.firstOrNull { it.fieldName == field.name }
            if (correction == null) field else field.copy(type = GQLNamedType(name = correction.servedType))
        },
    )
}
