package ktor.graphql.helpers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import io.ktor.http.formUrlEncode

/**
 * Removes all whitespace from a string. Useful when writing assertions for json strings.
 */
fun removeWhitespace(text: String): String {
    return text.replace("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex(), "")
}

fun urlString(vararg queryParams: Pair<String, String>): String {
    var route = "/graphql"
    if (queryParams.isNotEmpty()) {
        route += "?${queryParams.toList().formUrlEncode()}"
    }
    return route
}


fun Map<String, Any>.toJsonString(): String = jacksonObjectMapper().writeValueAsString(this)

fun GraphQLObjectType.Builder.Field(builderFunction: GraphQLFieldDefinition.Builder.() -> GraphQLFieldDefinition.Builder) = field {
    builderFunction.invoke(it)
}

fun GraphQLFieldDefinition.Builder.Argument(builderFunction: GraphQLArgument.Builder.() -> GraphQLArgument.Builder) = argument {
    builderFunction.invoke(it)
}