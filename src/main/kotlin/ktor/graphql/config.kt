package ktor.graphql

import graphql.ExecutionResult
import graphql.GraphQLError
import ktor.graphql.explorer.renderPlayground

typealias RenderExplorer = (data: Map<String, Any?>?) -> String

data class Config(
        val formatError: ((GraphQLError) -> Map<String, Any>) = { it.toSpecification() },
        val showExplorer: Boolean = false,
        val executionResult: ExecutionResult? = null,
        val renderExplorer: RenderExplorer = { renderPlayground() }
)