package ktor.graphql

import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import ktor.graphql.helpers.getRequest
import ktor.graphql.helpers.testGraphQLServer
import ktor.graphql.helpers.testResponse
import ktor.graphql.helpers.urlString
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals

object APITest : Spek({

    withTestApplication {

        var requestInSetupFunction: GraphQLRequest? = null

        testGraphQLServer { request ->
            requestInSetupFunction = request
            config {
                context = "testValue"
                rootValue = "testValue"
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


        describe("allows for performing multiple requests that are slow simultaneously") {


            runBlocking {
                val deferred = (0 until 2).map {
                    async {
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


