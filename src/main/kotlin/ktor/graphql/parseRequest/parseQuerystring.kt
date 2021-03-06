package ktor.graphql.parseRequest

import com.fasterxml.jackson.module.kotlin.readValue
import ktor.graphql.GraphQLRequest
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import ktor.graphql.HttpException
import ktor.graphql.HttpGraphQLError
import ktor.graphql.mapper

internal fun parseQueryString(params: Parameters): GraphQLRequest {
    val query = params["query"]
    val operationName = operationNameFromParams(params)
    val variables = variablesFromParams(params)

    return GraphQLRequest(query, operationName, variables)
}


private fun variablesFromParams(params:Parameters): Map<String, Any>? {

    val variables = params["variables"]

    return if (variables != null) {
        try {
            mapper.readValue(variables) as Map<String, Any>
        } catch (exception: Exception) {
            throw HttpException(HttpStatusCode.BadRequest, HttpGraphQLError("Variables are invalid JSON."))
        }
    } else {
        null
    }
}

private fun operationNameFromParams(params:Parameters): String? = params["operationName"]