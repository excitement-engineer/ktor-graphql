package ktor.graphql

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import ktor.graphql.helpers.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object GraphiQLTest : Spek({

    fun TestApplicationEngine.graphiQLServer() = testGraphQLServer {
        config {
            graphiql = true
        }
    }

    fun <R> graphiQLRequest(callback: TestApplicationRequest.() -> R) = withTestApplication {
        graphiQLServer()

        handleRequest {
            method = HttpMethod.Get
            addHeader(HttpHeaders.Accept, "text/html")
            callback(this)
        }
    }

    describe("graphiQL tests") {

        fun TestApplicationResponse.assertGraphiQLResponse(code: HttpStatusCode = HttpStatusCode.OK) {
            testCode(this, code)
            testContentType(this, "text/html; charset=UTF-8")
        }

        describe("does not render GraphiQL if no opt-in") {

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

        describe("presents GraphiQL when accepting HTML") {
            val call = graphiQLRequest {
                uri = urlString("query" to "{test}")
            }

            call.response.run {

                assertGraphiQLResponse()
                val content = content!!

                it("contains the query") {
                    assertContains(content, "{test}")
                }

                it("contains the javascript file") {
                    assertContains(content, "graphiql.min.js")
                }

                it("contains a pre-run response") {
                    assertContains(content, "response: JSON.stringify({\"data\":{\"test\":\"Hello World\"}}, null, 2)")
                }
            }
        }

        describe("contains a pre-run operation name within GraphiQL") {
            graphiQLRequest {
                uri = urlString(
                        "query" to "query A{a:test} query B{b:test}",
                        "operationName" to "B"
                )
            }.response.run {

                assertGraphiQLResponse()
                val content = content!!

                it("contains the graphQL json response") {
                    assertContains(content, "response: JSON.stringify({\"data\":{\"b\":\"Hello World\"}}, null, 2)")
                }

                it("contains the operation name") {
                    assertContains(content, "operationName: \"B\"")
                }
            }
        }

        describe("escapes HTML in queries within GraphiQL") {
            graphiQLRequest {
                uri = urlString(
                        "query" to "</script><script>alert(1)</script>",
                        "operationName" to "B"
                )
            }.response.run {
                assertGraphiQLResponse(HttpStatusCode.BadRequest)
                it("escapes HTML in queries within GraphiQL") {
                    assertDoesntContains(content!!, "</script><script>alert(1)</script>")
                }
            }
        }

        describe("escapes HTML in variables within GraphiQL") {
            graphiQLRequest {
                uri = urlString(
                        "query" to "query helloWho(${"$"}who: String){ test(who: ${"$"}who) }",
                        "variables" to """
                    {
                        "who": "</script><script>alert(1)</script>"
                    }
                    """
                )
            }.response.run {

                assertGraphiQLResponse()
                it("escapes HTML in variables within GraphiQL") {
                    assertDoesntContains(content!!, "</script><script>alert(1)</script>")
                }
            }
        }

        describe("GraphiQL renders the provided variables") {

            graphiQLRequest {
                uri = urlString(
                        "query" to "query helloWho(${"$"}who: String){ test(who: ${"$"}who) }",
                        "variables" to """
                            {
                                "who": "Dolly"
                            }
                            """
                )
            }.response.run {
                assertGraphiQLResponse()
                it("renders the variables") {
                    assertContains(content!!, "variables: JSON.stringify({\"who\":\"Dolly\"}, null, 2)")
                }
            }
        }

        describe("GraphiQL accepts an empty query") {
            graphiQLRequest {
                uri = urlString()
            }.response.run {
                assertGraphiQLResponse()
                it("contains an undefined response") {
                    assertContains(content!!, "response: undefined")
                }
            }
        }

        describe("GraphiQL accepts a mutation query - does not execute it") {
            graphiQLRequest {
                uri = urlString(
                        Pair("query", "mutation TestMutation { writeTest { test } }")
                )
            }.response.run {
                assertGraphiQLResponse()

                val response = content!!

                it("contains the mutation in the content") {
                    assertContains(response, "query: \"mutation TestMutation { writeTest { test } }\"")
                }

                it("contains an empty response") {
                    assertContains(response, "response: undefined")
                }
            }
        }

        describe("returns HTML if preferred") {
            withTestApplication {
                graphiQLServer()

                handleRequest {
                    uri = urlString("query" to "{test}")
                    addHeader(HttpHeaders.Accept, "text/html,application/json")
                }
            }

            graphiQLRequest {
                uri = urlString("query" to "{test}")
            }.response.run {
                assertGraphiQLResponse()
                it("contain the graphiql js file") {
                    assertContains(content!!, "graphiql.min.js")
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
                graphiQLServer()

                handleRequest {
                    uri = urlString("query" to "{test}")
                    addHeader(HttpHeaders.Accept, "application/json,text/html")
                }
            }.assertJSONTestQuery()
        }

        describe("prefers JSON if unknown accept") {
            withTestApplication {
                graphiQLServer()

                handleRequest {
                    uri = urlString("query" to "{test}")
                    addHeader(HttpHeaders.Accept, "unknown")
                }
            }.assertJSONTestQuery()
        }

        describe("prefers JSON if no header is specified") {
            withTestApplication {
                graphiQLServer()

                handleRequest {
                    uri = urlString("query" to "{test}")
                }
            }.assertJSONTestQuery()
        }

        describe("prefers JSON if explicitly requested raw response") {
            withTestApplication {
                graphiQLServer()

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
                graphiQLServer()

                handleRequest {
                    uri = urlString(
                            "query" to "{test}"
                    )
                    addHeader(HttpHeaders.Accept, "image/jpeg,text/html,application/json")
                }
            }.response.run {
                assertGraphiQLResponse()
                it("contains the expected response") {
                    assertContains(content!!, "graphiql.min.js")
                }
            }
        }
    }

})