package ktor.graphql

import graphql.ExceptionWhileDataFetching
import graphql.GraphqlErrorHelper
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import ktor.graphql.helpers.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ErrorHandlingTest : Spek({

    describe("it catches errors from the setup function") {

        withTestApplication {
            testGraphQLServer {
                throw Exception("Something went wrong")
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

    describe("handles field errors caught by graphql") {

        testResponse(
                call = getRequest {
                    uri = urlString("query" to "{ thrower }")
                },
                json = """
                {
                    "data": {
                        "thrower": null
                    },
                    "errors": [
                        {
                            "message": "Exception while fetching data (/thrower) : Throws!",
                            "locations":[{"line":1,"column":3}],
                            "path":["thrower"],
                            "extensions": {
                                "classification": "DataFetchingException"
                            }
                        }
                    ]
                }
                """
        )
    }

    describe("handles query errors from non-null top field errors") {
        testResponse(
                call = getRequest {
                    uri = urlString("query" to "{ nonNullThrower }")
                },
                code = HttpStatusCode.InternalServerError,
                json = """
                        {
                            "data": null,
                            "errors": [
                                {
                                    "message":"Exception while fetching data (/nonNullThrower) : Throws!",
                                    "locations":[{"line":1,"column":3}],
                                    "path":["nonNullThrower"],
                                    "extensions": {
                                        "classification": "DataFetchingException"
                                    }
                                }
                            ]
                        }
                    """
        )
    }

    describe("allows for custom error formatting to sanitize") {
        withTestApplication {
            testGraphQLServer {
                Config(formatError = { error ->
                    val message = if (error is ExceptionWhileDataFetching) {
                        error.exception.message
                    } else {
                        error.message
                    }
                    mapOf(
                            Pair("message", "Custom error format: $message")
                    )
                }
                )
            }
        }
    }

    describe("allows for custom error formatting to elaborate") {

        withTestApplication {
            testGraphQLServer {
                Config(formatError = { error ->
                    mapOf(
                            Pair("message", error.message),
                            Pair("locations", GraphqlErrorHelper.locations(error.locations)),
                            Pair("stack", "stack trace")
                    )
                }
                )
            }

            testResponse(
                    call = handleRequest {
                        uri = urlString("query" to "{ thrower }")
                    },
                    json = """
                        {
                            "data": {
                                "thrower": null
                            },
                            "errors": [
                                {
                                    "message":"Exception while fetching data (/thrower) : Throws!",
                                    "locations":[{"line":1,"column":3}],
                                    "stack":"stack trace"
                                }
                            ]
                        }
                        """
            )
        }
    }

    describe("handles syntax errors caught by GraphQL") {
        testResponse(
                call = getRequest {
                    uri = urlString("query" to "syntaxerror")
                },
                code = HttpStatusCode.BadRequest,
                json = """
                    {
                        "errors": [
                            {
                                "message": "Invalid Syntax : offending token 'syntaxerror' at line 1 column 1",
                                "locations": [{ "line": 1, "column": 1 }],
                                "extensions": { 
                                    "classification": "InvalidSyntax"
                                }
                            }
                        ]
                    }
                """.trimIndent()
        )
    }

    describe("handles error caused by a lack of query") {
        testResponse(
                call = getRequest {
                    addHeader(HttpHeaders.ContentType, "application/json")
                    uri = urlString()
                },
                code = HttpStatusCode.BadRequest,
                json = """
                    {
                        "errors": [
                            {
                                "message": "Must provide query string."
                            }
                        ]
                    }
                    """
        )
    }

    describe("handles invalid JSON bodies") {
        testResponse(
                call = postJSONRequest {
                    setBody("[]")
                },
                code = HttpStatusCode.BadRequest,
                json = """
                    {
                        "errors": [
                            {
                                "message": "POST body sent invalid JSON."
                            }
                         ]
                    }
                    """
        )
    }

    describe("handles incomplete JSON bodies") {
        testResponse(
                call = postJSONRequest {
                    setBody("""{"query":""")
                },
                code = HttpStatusCode.BadRequest,
                json = """
                    {
                        "errors": [
                            {
                                "message": "POST body sent invalid JSON."
                            }
                         ]
                    }
                    """
        )
    }

    describe("handles plain post text") {
        testResponse(
                call = postRequest {
                    addHeader(HttpHeaders.ContentType, "text/plain")
                    setBody("query helloWho(${"$"}who: String){ test(who: ${"$"}who) }")
                },
                code = HttpStatusCode.BadRequest,
                json = """
                    {
                        "errors": [
                            {
                                "message": "Must provide query string."
                            }
                        ]
                    }
                    """
        )
    }

    describe("handles poorly formed variables") {
        testResponse(
                call = postRequest {
                    uri = urlString(
                            "variables" to "who:you",
                            "query" to "query helloWho(${"$"}who: String){ test(who: ${"$"}who) }"
                    )
                },
                code = HttpStatusCode.BadRequest,
                json = """
                    {
                        "errors": [
                            {
                                "message": "Variables are invalid JSON."
                            }
                         ]
                    }
                    """
        )
    }

    describe("allows for custom error formatting of poorly formed requests") {
        withTestApplication {
            testGraphQLServer {
                Config(formatError = { error ->
                    mapOf(Pair("message", "Custom error format: ${error.message}"))
                }
                )
            }

            testResponse(
                    call = handleRequest {
                        uri = urlString(
                                Pair("variables", "who:you"),
                                Pair("query", "query helloWho(${"$"}who: String){ test(who: ${"$"}who) }")
                        )
                        method = HttpMethod.Post
                    },
                    code = HttpStatusCode.BadRequest,
                    json = """
                    {
                        "errors": [
                            {
                                "message": "Custom error format: Variables are invalid JSON."
                            }
                         ]
                    }
                    """
            )
        }
    }

    describe("handles invalid variables") {
        testResponse(
                call = postJSONRequest {
                    setBody("""
                        {
                            "query": "query helloWho(${"$"}value: Boolean){ testBoolean(value: ${"$"}value) }",
                            "variables": {
                                "value": ["Dolly", "Jonty"]
                            }
                        }
                    """)
                },
                code = HttpStatusCode.InternalServerError,
                json = """
                {
                    "data": null,
                    "errors": [{
                        "message": "Variable 'value' has an invalid value : Expected type 'Boolean' but was 'ArrayList'.",
                        "locations": [{
                            "line": 1,
                            "column": 16
                        }],
                        "extensions": {
                            "classification": "ValidationError"
                        }
                    }]
                }
                """
        )
    }
})

