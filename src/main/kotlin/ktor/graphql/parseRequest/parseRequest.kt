package ktor.graphql.parseRequest

import ktor.graphql.GraphQLRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ktor.graphql.HttpException
import ktor.graphql.HttpGraphQLError
import ktor.graphql.parseRequest.parseQueryString
import kotlin.text.Charsets

/**
 * See https://graphql.org/learn/serving-over-http/
 */
internal suspend fun parseGraphQLRequest(call: ApplicationCall): GraphQLRequest {
    return try {
        parseRequest(call)
    } catch (exception: Exception) {
        if (exception !is HttpException) {
            throw HttpException(HttpStatusCode.BadRequest, HttpGraphQLError("The GraphQL query could not be parsed"))
        } else {
            throw exception
        }
    }
}

private suspend fun parseRequest(call: ApplicationCall): GraphQLRequest {

    val body = call.receiveTextWithCorrectEncoding()
    val contentType = call.request.contentType()
    val bodyRequest = parseBody(body, contentType)


    val parameters = call.parameters
    val urlRequest = parseQueryString(parameters)

    val query = urlRequest.query ?: bodyRequest.query
    val variables = urlRequest.variables ?: bodyRequest.variables
    val operationName = urlRequest.operationName ?: bodyRequest.operationName

    return GraphQLRequest(query = query, variables = variables, operationName = operationName)
}

/**
 * Receive the request as String.
 * This is a workaround for issue https://github.com/ktorio/ktor/issues/384.
 *
 * ktor defaults to ISO-8859-1 instead of UTF-8 for the encoding. This may cause issues
 * for certain characters in the body. To overcome this we set the default to UTF-8 unless specified otherwise.
 */
private suspend fun ApplicationCall.receiveTextWithCorrectEncoding(): String {

    // Need to prevent from blocking
    return withContext(Dispatchers.IO) {
        val charset = request.contentCharset()

        val body = receiveStream().readBytes().toString(charset ?: Charsets.UTF_8)
        body
    }
}
