package ktor.graphql

import graphQLRoute.removeWhitespace
import graphQLRoute.urlString
import io.ktor.application.Application
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.routing
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.Suite
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
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
                        "query" to """
                              query helloYou { test(who: "You"), ...shared }
                              query helloWorld { test(who: "World"), ...shared }
                              query helloDolly { test(who: "Dolly"), ...shared }
                              fragment shared on Query {
                                shared: test(who: "Everyone")
                              }
                            """,
                        "operationName" to "helloWorld"
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

        describe("it reports validation errors") {
            val call = getRequest {
                uri = urlString("query" to "{ test, unknownOne, unknownTwo }")
            }

            testResponse(call,
                    code = HttpStatusCode.BadRequest,
                    json = """
                    {
                      "errors": [
                        {
                          "message": "Validation error of type FieldUndefined: Field 'unknownOne' in type 'Query' is undefined @ 'unknownOne'",
                          "locations": [
                            {
                              "line": 1,
                              "column": 9
                            }
                          ]
                        },
                        {
                          "message": "Validation error of type FieldUndefined: Field 'unknownTwo' in type 'Query' is undefined @ 'unknownTwo'",
                          "locations": [
                            {
                              "line": 1,
                              "column": 21
                            }
                          ]
                        }
                      ]
                    }
                    """)
        }

        describe("errors when missing the operation name") {
            testResponse(
                    call = getRequest {
                        uri = urlString("query" to """
                            query TestQuery { test }
                            mutation TestMutation { writeTest { test } }
                          """
                        )
                    },
                    code = HttpStatusCode.BadRequest,
                    json = """
                        {
                          "errors": [
                            {
                              "message": "Must provide operation name if query contains multiple operations."
                            }
                          ]
                        }
                    """
            )
        }

        describe("errors when sending a mutation via GET") {
            testResponse(
                    call = getRequest {
                        uri = urlString("query" to "mutation TestMutation { writeTest { test } }")
                    },
                    code = HttpStatusCode.MethodNotAllowed,
                    json = """
                        {
                            "errors": [
                                {
                                    "message": "Can only perform a mutation operation from a POST request."
                                }
                            ]
                        }
                    """
            )
        }

        describe("errors when selecting a mutation via a GET") {
            testResponse(
                    call = getRequest {
                        uri = urlString(
                                "operationName" to "TestMutation",
                                "query" to """
                                    query TestQuery { test }
                                    mutation TestMutation { writeTest { test } }
                                """
                        )
                    },
                    code = HttpStatusCode.MethodNotAllowed,
                    json = """
                        {
                            "errors": [{
                                "message": "Can only perform a mutation operation from a POST request."
                            }]
                        }
                    """
            )
        }

        describe("allows a mutation to exists within a GET") {
            testResponse(
                    call = getRequest {
                        uri = urlString(
                                "operationName" to "TestQuery",
                                "query" to """
                                    mutation TestMutation { writeTest { test } }
                                    query TestQuery { test }
                                """
                        )
                    },
                    json = """
                        {
                          "data": {
                            "test": "Hello World"
                          }
                        }
                    """
            )
        }
    }

    describe("API tests") {

        withTestApplication {

            var requestInSetupFunction: GraphQLRequest? = null

            application.routing {
                graphQL(urlString(), schema) { request ->
                    println(request)
                    requestInSetupFunction = request
                    config {
                        context = "testValue"
                        rootValue = "testValue"
                    }
                }
            }

            describe("allows passing in a context") {

                testResponse(
                        call = handleRequest {
                            uri = urlString(
                                    "operationName" to "TestQuery",
                                    "query" to "query TestQuery { context }"
                            )
                        },
                        json = """
                            {
                              "data": {
                                "context": "testValue"
                              }
                            }
                        """
                )

            }

            describe("allows passing in a root value") {

                testResponse(
                        call = handleRequest {
                            uri = urlString(
                                    "operationName" to "TestQuery",
                                    "query" to "query TestQuery { rootValue }"
                            )
                        },
                        json = """
                            {
                              "data": {
                                "rootValue": "testValue"
                              }
                            }
                        """
                )
            }

            describe("it provides a setup function with arguments") {
                testResponse(
                        call = handleRequest {
                            uri = urlString("query" to "{ test }")
                        },
                        json = "{\"data\":{\"test\":\"Hello World\"}}"
                )

                it("has the correct setup function") {
                    assertEquals(
                            expected = GraphQLRequest(query = "{ test }"),
                            actual = requestInSetupFunction
                    )
                }
            }
        }

        describe("it catches errors from the setup function") {

            withTestApplication {
                application.routing {
                    graphQL(urlString(), schema) {
                        throw Exception("Something went wrong")
                    }
                }

                testResponse(
                        call = handleRequest {
                            uri = urlString("query" to "{ test }")
                        },
                        code = HttpStatusCode.InternalServerError,
                        json = """
                            {
                                "errors": [{ "message": "Something went wrong" }]
                            }
                        """
                )
            }
        }
    }

    describe("parallel requests") {

        describe("allows for performing multiple requests that are slow simultaneously") {


            runBlocking {
                val deferred = (0 until 2).map {
                    GlobalScope.async {
                        getRequest {
                            uri = urlString(Pair("query", "{ slow }"))
                        }
                    }

                }

                deferred.awaitAll().forEach { call ->
                    testResponse(
                            call = call,
                            json = "{\"data\":{\"slow\":\"hello\"}}"
                    )
                }
            }
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
            expect(code) { status() }
        }

        it("has expected json") {
            expect(removeWhitespace(json)) { content }
        }
    }
}