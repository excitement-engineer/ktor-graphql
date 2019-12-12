package ktor.graphql

import graphql.GraphQLError

internal fun ExecutionResultData.formatResult(
        formatError: ((GraphQLError) -> Map<String, Any>)
): Map<String, Any?> {

    val executionResult = result

    val data = executionResult.getData<Any>()

    val errors = executionResult.errors

    val responseMap = mutableMapOf<String, Any?>()

    if (isDataPresent) {
        responseMap["data"] = data
    }

    if (errors.isNotEmpty()) {

        val outputError = errors.map { error -> formatError(error) }

        responseMap["errors"] = outputError
    }

    if (executionResult.extensions != null) {
        responseMap["extensions"] = executionResult.extensions
    }

    return responseMap
}