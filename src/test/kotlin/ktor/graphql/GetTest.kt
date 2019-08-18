package ktor.graphql

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


object GetTest : Spek({

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

})