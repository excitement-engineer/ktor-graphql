package ktor.graphql.helpers

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.*
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication

fun <R> testApp(callback: TestApplicationEngine.() -> R) = withTestApplication(
        Application::testGraphQLRoute,
        callback
)
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

fun TestApplicationRequest.setJsonBody(vararg body: Pair<String, Any>) {
    setBody(body.toMap().toJsonString())
}