package graphQLRoute

import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.formUrlEncode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.withTestApplication
import ktor.graphql.testGraphQLRoute
import org.spekframework.spek2.style.specification.Suite
import kotlin.test.expect

/**
 * Removes all whitespace from a string. Useful when writing assertions for json strings.
 */
fun removeWhitespace(text: String): String {
    return text.replace("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex(), "")
}

fun urlString(vararg queryParams: Pair<String, String>): String {
    var route = "/graphql"
    if (queryParams.isNotEmpty()) {
        route +="?${queryParams.toList().formUrlEncode()}"
    }
    return route
}

fun stringify(vararg queryParams: Pair<String, String>): String = queryParams.toList().formUrlEncode()

fun <R> testApp(callback: TestApplicationEngine.() -> R) = withTestApplication(Application::testGraphQLRoute, callback)

fun <R> request(callback: TestApplicationRequest.() -> R) = testApp {
    handleRequest {
        callback(this)
    }
}

fun <R> getRequest(callback: TestApplicationRequest.() -> R) = testApp {
    handleRequest {
        method = HttpMethod.Get
        callback(this)
    }
}


fun Suite.testResponse(
        call: TestApplicationCall,
        code: HttpStatusCode = HttpStatusCode.OK,
        json: String
) {

    call.response.apply {
        it("return ${code.value} status code") {
            expect(code) { status() }
        }

        it("has expected json") {
            expect(removeWhitespace(json)) { content }
        }
    }
}