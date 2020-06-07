package ktor.graphql

import graphql.ExecutionResult
import graphql.GraphQLError
import ktor.graphql.explorer.renderPlayground

typealias RenderExplorer = (data: Map<String, Any?>?) -> String
typealias ExecuteRequest = (() -> ExecutionResult)
typealias FormatError =  ((GraphQLError) -> Map<String, Any>)

data class Config(
        val formatError: FormatError = { it.toSpecification() },
        val showExplorer: Boolean = false,
        val executeRequest: ExecuteRequest? = null,
        val renderExplorer: RenderExplorer = { renderPlayground() }
)