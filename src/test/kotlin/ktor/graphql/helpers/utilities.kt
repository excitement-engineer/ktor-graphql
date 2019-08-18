package ktor.graphql.helpers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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