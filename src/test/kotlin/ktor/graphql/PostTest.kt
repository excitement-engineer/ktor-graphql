package ktor.graphql

import io.ktor.http.HttpHeaders
import io.ktor.server.testing.setBody
import ktor.graphql.helpers.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object PostTest : Spek({

    describe("allows POST with JSON encoding") {

        val query = """
        query IntrospectionQuery {
             __schema {
                queryType {
                    name
                }
                mutationType {
                    name
                }
                types {
                    ...FullType
                }
                directives {
                    name
                    description
                    locations
                    args {
                        ...InputValue
                    }
                }
            }
        }

        fragment FullType on __Type {
            kind
            name
            description
            fields(includeDeprecated: true) {
                name
                description
                args {
                    ...InputValue
                }
                type {
                    ...TypeRef
                }
                isDeprecated
                deprecationReason
            }
            inputFields {
                ...InputValue
            }
            interfaces {
                ...TypeRef
            }
            enumValues(includeDeprecated: true) {
                  name
                  description
                  isDeprecated
                  deprecationReason
              }
              possibleTypes {
                ...TypeRef
              }
            }

            fragment InputValue on __InputValue {
                name
                description
                type {
                    ...TypeRef
                }
                defaultValue
            }

            fragment TypeRef on __Type {
                kind
                name
                ofType {
                    kind
                    name
                    ofType {
                        kind
                        name
                        ofType {
                            kind
                            name
                            ofType {
                                kind
                                name
                                ofType {
                                    kind
                                    name
                                    ofType {
                                        kind
                                        name
                                        ofType {
                                            kind
                                            name
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        """

        postJSONRequest {
            setJsonBody("query" to query)
        }.response.run {
            testCode(this)
        }
    }

    describe("allows sending a mutation via POST") {

        val mutation = """
            mutation TestMutation {
                writeTest {
                    test
                }
            }
        """

        testResponse(
                call = postJSONRequest {
                    setJsonBody("query" to mutation)
                },
                json = """
                {
                    "data": {
                        "writeTest":{
                            "test":"Hello World"
                        }
                    }
                }
            """
        )
    }

    describe("support POST JSON query with JSON variables") {

        val query = """
            query helloWho(${"$"}who: String) {
                test(who: ${"$"}who)
            }
        """

        val variables = mapOf(
                "who" to "Dolly"
        )

        testResponse(
                call = postJSONRequest {
                    setJsonBody(
                            "query" to query,
                            "variables" to variables
                    )
                },
                json = """
                {
                    "data": {
                        "test":"Hello Dolly"
                    }
                }
                """
        )
    }

    describe("support POST JSON with GET variable values") {

        val variables = """
            {
                "who": "Dolly"
            }
            """

        val query = """
            query helloWho(${"$"}who: String){
                test(who: ${"$"}who)
            }
        """

        testResponse(
                call = postJSONRequest {
                    uri = urlString("variables" to variables)

                    setJsonBody("query" to query)
                },
                json = """
                {
                    "data": {
                        "test":"Hello Dolly"
                    }
                }
                """
        )
    }

    describe("allows POST with operation name") {

        val query = """
             query helloYou {
                test(who: "You")
             }
             query helloWorld {
                test(who: "World")
             }
          """

        testResponse(
                call = postJSONRequest {
                    setJsonBody(
                            "query" to query,
                            "operationName" to "helloWorld"
                    )
                },
                json = """
            {
                "data": {
                    "test":"Hello World"
                }
            }
            """
        )
    }

    describe("allows POST with GET operation name") {

        val query = """
            query helloYou {
                test(who: "You")
            }
            query helloWorld {
                test(who: "World")
            }
        """

        testResponse(
                call = postJSONRequest {
                    uri = urlString(Pair("operationName", "helloWorld"))
                    setJsonBody("query" to query)
                },
                json = """
                    {
                        "data": {
                            "test":"Hello World"
                        }
                    }
                """
        )
    }

    describe("supports POST raw text query with GET variable values") {

        val query = """
            query helloWho(${"$"}who: String){
                test(who: ${"$"}who)
            }
        """.trimIndent()

        testResponse(
                call = postRequest {
                    uri = urlString(
                            "variables" to """
                                {
                                    "who": "Dolly"
                                }
                            """,
                            "operationName" to "helloWho"
                    )
                    setBody(query)
                    addHeader(HttpHeaders.ContentType, "application/graphql")
                },
                json = """
                {
                    "data": {
                        "test":"Hello Dolly"
                    }
                }
                """
        )
    }

    describe("supports application/json POST with content-subtypes") {
        val query = """
            query helloWho(${"$"}who: String) {
                test(who: ${"$"}who)
            }
        """

        val variables = mapOf(
                "who" to "Dolly"
        )

        testResponse(
                call = postRequest {
                    setJsonBody(
                            "query" to query,
                            "variables" to variables
                    )
                    addHeader(HttpHeaders.ContentType, "application/json; charset=UTF-8")
                },
                json = """
                {
                    "data": {
                        "test":"Hello Dolly"
                    }
                }
                """
        )
    }
})