package ktor.graphql

import graphql.ExecutionResult
import graphql.GraphQLError

fun config(block: GraphQLRouteConfigBuilder.() -> Unit) = GraphQLRouteConfigBuilder().apply(block).build()

class GraphQLRouteConfigBuilder {
    var context: Any? = null
    var rootValue: Any? = null
    var formatError: ((GraphQLError) -> Map<String, Any>) = { it.toSpecification() }
    var graphiql: Boolean = false
    var executionResult: ExecutionResult? = null
    internal fun build(): GraphQLRouteConfig {
        return GraphQLRouteConfig(context, rootValue, formatError, graphiql, executionResult)
    }
}

data class GraphQLRouteConfig(
        val context: Any? = null,
        val rootValue: Any? = null,
        val formatError: ((GraphQLError) -> Map<String, Any>) = { it.toSpecification() },
        val graphiql: Boolean = false,
        val executionResult: ExecutionResult? = null
)