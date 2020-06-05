package ktor.graphql

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.uri
import io.ktor.server.testing.*
import ktor.graphql.helpers.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNull

object GraphiQLTest : Spek({

    val explorerHTML = "Explorer HTML"

    fun TestApplicationEngine.explorerRequest(uri: String) = handleRequest {
        method = HttpMethod.Get
        addHeader(HttpHeaders.Accept, "text/html")
        this.uri = uri
    }

    fun TestApplicationEngine.explorerServer() = testGraphQLServer {
        Config(
                showExplorer = true,
                renderExplorer = { data -> explorerHTML }
        )
    }

    fun <R> explorerRequest(callback: TestApplicationRequest.() -> R) = withTestApplication {
        explorerServer()

        handleRequest {
            method = HttpMethod.Get
            addHeader(HttpHeaders.Accept, "text/html")
            callback(this)

        }
    }

    describe("Explorer tests") {

        fun TestApplicationResponse.assertExplorerResponse(code: HttpStatusCode = HttpStatusCode.OK) {
            testCode(this, code)
            testContentType(this, "text/html; charset=UTF-8")
        }

        describe("does not render explorer if no opt-in") {

            val call = getRequest {
                uri = urlString("query" to " { test } ")
                addHeader(HttpHeaders.Accept, "text/html")
            }
            testResponse(
                    call = call,
                    json = "{\"data\":{\"test\":\"Hello World\"}}",
                    contentType = "application/json; charset=UTF-8"
            )
        }

        describe("presents explorer when accepting HTML") {

            var queryData: Map<String, Any?>? = null
            val response = withTestApplication {
                testGraphQLServer {
                    Config(
                            showExplorer = true,
                            renderExplorer = { data ->
                                queryData = data
                                explorerHTML
                            }
                    )
                }

                val req = explorerRequest(urlString("query" to "{test}"))

                req.response
            }

            response.assertExplorerResponse()

            it("renders") {
                assertContains(response.content!!, explorerHTML)
            }

            it("passes the data") {
                assertEquals(
                        expected = mapOf("data" to mapOf("test" to "Hello World")),
                        actual = queryData
                )
            }
        }


        describe("GraphiQL accepts a mutation query - does not execute it") {

            var mutationData: Map<String, Any?>? = null
            val response = withTestApplication {
                testGraphQLServer {
                    Config(
                            showExplorer = true,
                            renderExplorer = { data ->
                                mutationData = data
                                explorerHTML
                            }
                    )
                }

                val req = explorerRequest(urlString(
                        Pair("query", "mutation TestMutation { writeTest { test } }")
                ))

                req.response
            }

            response.assertExplorerResponse()

            it("renders") {
                assertEquals(explorerHTML, response.content)
            }
            it("contains an empty response") {
                assertNull(mutationData)
            }
        }

        describe("returns HTML if preferred") {
            withTestApplication {
                explorerServer()

                handleRequest {
                    uri = urlString("query" to "{test}")
                    addHeader(HttpHeaders.Accept, "text/html,application/json")
                }
            }

            explorerRequest {
                uri = urlString("query" to "{test}")
            }.response.run {
                assertExplorerResponse()
                it("contain the graphiql js file") {
                    assertEquals(content, explorerHTML)
                }
            }
        }

        fun TestApplicationCall.assertJSONTestQuery() {
            testResponse(
                    this,
                    json = "{\"data\":{\"test\":\"Hello World\"}}",
                    contentType = "application/json; charset=UTF-8"
            )
        }

        describe("returns JSON if preferred") {
            withTestApplication {
                explorerServer()

                handleRequest {
                    uri = urlString("query" to "{test}")
                    addHeader(HttpHeaders.Accept, "application/json,text/html")
                }
            }.assertJSONTestQuery()
        }

        describe("prefers JSON if unknown accept") {
            withTestApplication {
                explorerServer()

                handleRequest {
                    uri = urlString("query" to "{test}")
                    addHeader(HttpHeaders.Accept, "unknown")
                }
            }.assertJSONTestQuery()
        }

        describe("prefers JSON if no header is specified") {
            withTestApplication {
                explorerServer()

                handleRequest {
                    uri = urlString("query" to "{test}")
                }
            }.assertJSONTestQuery()
        }

        describe("prefers JSON if explicitly requested raw response") {
            withTestApplication {
                explorerServer()

                handleRequest {
                    uri = urlString(
                            "query" to "{test}",
                            "raw" to ""
                    )
                    addHeader(HttpHeaders.Accept, "text/html")
                }
            }.assertJSONTestQuery()
        }

        describe("prefers html even if the first accept header is different") {
            withTestApplication {
                explorerServer()

                handleRequest {
                    uri = urlString(
                            "query" to "{test}"
                    )
                    addHeader(HttpHeaders.Accept, "image/jpeg,text/html,application/json")
                }
            }.response.run {
                assertExplorerResponse()
                it("contains the expected response") {
                    assertEquals(content, explorerHTML)
                }
            }
        }
    }

})