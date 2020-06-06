package ktor.graphql

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.uri
import io.ktor.routing.Routing
import io.ktor.server.testing.*
import ktor.graphql.helpers.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNull

object ExplorerTest : Spek({

    val explorerHTML = "Explorer HTML"

    fun TestApplicationEngine.explorerRequest(cb: (TestApplicationRequest.() -> Unit)) = handleRequest {
        method = HttpMethod.Get
        addHeader(HttpHeaders.Accept, "text/html")
        cb(this)
    }

    fun TestApplicationEngine.explorerServer(cb: (data: Map<String, Any?>?) -> Unit = {}) = testGraphQLServer {
        Config(
                showExplorer = true,
                renderExplorer = { data ->
                    cb(data)
                    explorerHTML
                }
        )
    }

    val testQueryURI = urlString("query" to "{test}")

    describe("Explorer tests") {

        fun TestApplicationResponse.assertExplorerResponse(code: HttpStatusCode = HttpStatusCode.OK) {
            testCode(this, code)
            testContentType(this, "text/html; charset=UTF-8")

            it("renders the html") {
                assertEquals(explorerHTML, content)
            }
        }

        fun TestApplicationCall.assertJSONTestQuery() {
            testResponse(
                    this,
                    json = "{\"data\":{\"test\":\"Hello World\"}}",
                    contentType = "application/json; charset=UTF-8"
            )
        }

        describe("does not render explorer if no opt-in") {

            val call = getRequest {
                uri = testQueryURI
                addHeader(HttpHeaders.Accept, "text/html")
            }

            call.assertJSONTestQuery()
        }

        describe("presents explorer when accepting HTML") {

            var queryData: Map<String, Any?>? = null
            val response = withTestApplication {
                explorerServer { data -> queryData = data }

                val req = explorerRequest {
                    uri = testQueryURI
                }

                req.response
            }

            response.assertExplorerResponse()

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
                explorerServer { data ->
                    mutationData = data
                }

                val req = explorerRequest {
                    uri = urlString("query" to "mutation TestMutation { writeTest { test } }")
                }

                req.response
            }

            response.assertExplorerResponse()

            it("contains an empty response") {
                assertNull(mutationData)
            }
        }

        describe("returns HTML if preferred") {
            val response = withTestApplication {
                explorerServer()

                val req = handleRequest {
                    uri = testQueryURI
                    addHeader(HttpHeaders.Accept, "text/html,application/json")
                }

                req.response
            }

            response.assertExplorerResponse()
        }

        describe("returns JSON if preferred") {
            withTestApplication {
                explorerServer()

                handleRequest {
                    uri = testQueryURI
                    addHeader(HttpHeaders.Accept, "application/json,text/html")
                }
            }.assertJSONTestQuery()
        }

        describe("prefers JSON if unknown accept") {
            withTestApplication {
                explorerServer()

                handleRequest {
                    uri = testQueryURI
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
            val response = withTestApplication {
                explorerServer()

                val req = handleRequest {
                    uri = testQueryURI
                    addHeader(HttpHeaders.Accept, "image/jpeg,text/html,application/json")
                }

                req.response
            }

            response.assertExplorerResponse()
        }
    }
})