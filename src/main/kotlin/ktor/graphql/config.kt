package ktor.graphql

import graphql.ExecutionResult
import graphql.GraphQLError

fun config(block: GraphQLRouteConfigBuilder.() -> Unit) = GraphQLRouteConfigBuilder().apply(block).build()

class GraphQLRouteConfigBuilder {
    var formatError: ((GraphQLError) -> Map<String, Any>) = { it.toSpecification() }
    var graphiql: Boolean = false
    var executionResult: ExecutionResult? = null
    internal fun build(): GraphQLRouteConfig {
        return GraphQLRouteConfig(formatError, graphiql, executionResult)
    }
}

data class GraphQLRouteConfig(
        val formatError: ((GraphQLError) -> Map<String, Any>) = { it.toSpecification() },
        val graphiql: Boolean = false,
        val executionResult: ExecutionResult? = null
)