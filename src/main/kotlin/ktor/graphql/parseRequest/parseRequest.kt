package ktor.graphql.parseRequest

import ktor.graphql.GraphQLRequest
import io.ktor.application.ApplicationCall
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.utils.io.charsets.*
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
 * If there is no Content-Type in the HTTP header specified use ISO_8859_1 as default charset, see https://www.w3.org/International/articles/http-charset/index#charset.
 * But use UTF-8 as default charset for application/json, see https://tools.ietf.org/html/rfc4627#section-3
 */
private suspend fun ApplicationCall.receiveTextWithCorrectEncoding(): String {
    fun ContentType.defaultCharset(): Charset = when (this) {
        ContentType.Application.Json -> Charsets.UTF_8
        else -> Charsets.ISO_8859_1
    }

    val contentType = request.contentType()
    val suitableCharset = contentType.charset() ?: contentType.defaultCharset()
    return receiveStream().bufferedReader(charset = suitableCharset).readText()
}
