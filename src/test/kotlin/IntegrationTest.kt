import graphQLRoute.removeWhitespace
import graphQLRoute.urlString
import io.ktor.application.Application
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.withTestApplication
import ktor.graphql.testGraphQLRoute
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.Suite
import org.spekframework.spek2.style.specification.describe
import kotlin.test.expect

object IntegrationTest : Spek({

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
    describe("GET call") {


        describe("with query param") {
            val call = getRequest {
                uri = urlString(Pair("query", "{ test }"))
            }

            testResponse(
                    call,
                    json = "{\"data\":{\"test\":\"Hello World\"}}"
            )
        }

        describe("with variable values") {

            val call = getRequest {
                uri = urlString(
                        Pair("query", "query helloWho(\$who: String){ test(who: \$who) }"),
                        Pair("variables", "{ \"who\": \"Dolly\" }")
                )
            }

            testResponse(
                    call,
                    json = "{\"data\":{\"test\":\"Hello Dolly\"}}"
            )
        }

        describe("with operation name") {
            val call = getRequest {
                uri = urlString(
                        Pair("query", """
                              query helloYou { test(who: "You"), ...shared }
                              query helloWorld { test(who: "World"), ...shared }
                              query helloDolly { test(who: "Dolly"), ...shared }
                              fragment shared on Query {
                                shared: test(who: "Everyone")
                              }
                            """
                        ),
                        Pair("operationName", "helloWorld")
                )
            }

            testResponse(
                    call,
                    json = """
                    {
                        "data": {
                            "test": "Hello World",
                            "shared": "Hello Everyone"
                        }
                    }
                    """
            )
        }

        describe("with content-type application/json") {
            val call = getRequest {
                uri = urlString(Pair("query", "{ test }"))
                addHeader(HttpHeaders.ContentType, "application/json")
            }

            testResponse(
                    call,
                    json = "{\"data\":{\"test\":\"Hello World\"}}"
            )
        }
    }
})


fun Suite.testResponse(
        call: TestApplicationCall,
        code: HttpStatusCode = HttpStatusCode.OK,
        json: String
) {

    call.response.apply {
        it("return ${code.value} status code") {
            expect(HttpStatusCode.OK) { status() }
        }

        it("has expected json") {
            expect(removeWhitespace(json)) { content }
        }
    }
}