package ktor.graphql

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import ktor.graphql.helpers.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNull

object GraphiQLTest : Spek({

    val explorerHTML = "Explorer HTML"

    var dataInExplorer: Map<String, Any?>? = null

    fun TestApplicationEngine.explorerServer() = testGraphQLServer {
        Config(
                showExplorer = true,
                renderExplorer = { data ->
                    dataInExplorer = data
                    explorerHTML
                }
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
            val call = explorerRequest {
                uri = urlString("query" to "{test}")
            }

            call.response.run {

                assertExplorerResponse()
                val content = content!!

                it("renders") {
                    assertContains(content, explorerHTML)
                }


            }

            it("passes the data") {
                assertEquals(
                        expected = mapOf("data" to mapOf("test" to "Hello World")),
                        actual = dataInExplorer
                )
            }
        }


        describe("GraphiQL accepts a mutation query - does not execute it") {
            explorerRequest {
                uri = urlString(
                        Pair("query", "mutation TestMutation { writeTest { test } }")
                )
            }.response.run {
                assertExplorerResponse()

                it("renders") {
                    assertEquals(explorerHTML, content)
                }
                it("contains an empty response") {
                    assertNull(dataInExplorer)
                }
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