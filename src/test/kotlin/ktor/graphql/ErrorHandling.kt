package ktor.graphql

import graphQLRoute.getRequest
import graphQLRoute.testGraphQLServer
import graphQLRoute.testResponse
import graphQLRoute.urlString
import graphql.ExceptionWhileDataFetching
import graphql.GraphQLError
import graphql.GraphqlErrorHelper
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.withTestApplication
import io.ktor.util.pipeline.PipelineContext
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ErrorHandling : Spek({

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
                            "path":["thrower"]
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
                                    "path":["nonNullThrower"]
                                }
                            ]
                        }
                    """
        )
    }

    describe("allows for custom error formatting to sanitize") {
        withTestApplication {
            testGraphQLServer {
                config {
                    formatError = {
                        val message = if (this is ExceptionWhileDataFetching) {
                            exception.message
                        } else {
                            message
                        }
                        mapOf(
                                Pair("message", "Custom error format: $message")
                        )
                    }
                }
            }
        }
    }

    describe("allows for custom error formatting to elaborate") {

        withTestApplication {
            testGraphQLServer {
                config {
                    formatError = {
                        mapOf(
                                Pair("message", message),
                                Pair("locations", GraphqlErrorHelper.locations(locations)),
                                Pair("stack", "stack trace")
                        )
                    }
                }
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
        getRequest {
            uri = urlString("query" to "synxtaxerror")
        }
    }

})

