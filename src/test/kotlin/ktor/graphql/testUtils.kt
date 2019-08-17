package graphQLRoute

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.formUrlEncode
import io.ktor.routing.routing
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.pipeline.PipelineContext
import ktor.graphql.*
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

fun <R> postRequest(callback: TestApplicationRequest.() -> R) = testApp {
    handleRequest {
        uri = urlString()
        method = HttpMethod.Post
        callback(this)
    }
}

fun <R> postJSONRequest(callback: TestApplicationRequest.() -> R) = postRequest {
    addHeader(HttpHeaders.ContentType, "application/json")
    callback(this)
}


fun <R> getRequest(callback: TestApplicationRequest.() -> R) = testApp {
    handleRequest {
        method = HttpMethod.Get
        callback(this)
    }
}

fun TestApplicationEngine.testGraphQLServer(
        setup: (PipelineContext<Unit, ApplicationCall>.(GraphQLRequest) -> GraphQLRouteConfig)? = null
) = application.routing {
    graphQL(urlString(), schema, setup)
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