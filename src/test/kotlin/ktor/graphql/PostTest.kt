package ktor.graphql

import io.ktor.http.HttpHeaders
import io.ktor.server.testing.setBody
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object PostTest : Spek({

    describe("allows POST with JSON encoding") {
        postJSONRequest {
            setBody("""
                {
                	"query": "\n  query IntrospectionQuery {\n    __schema {\n      queryType { name }\n      mutationType { name }\n      types {\n        ...FullType\n      }\n      directives {\n        name\n        description\n        locations\n        args {\n          ...InputValue\n        }\n      }\n    }\n  }\n\n  fragment FullType on __Type {\n    kind\n    name\n    description\n    fields(includeDeprecated: true) {\n      name\n      description\n      args {\n        ...InputValue\n      }\n      type {\n        ...TypeRef\n      }\n      isDeprecated\n      deprecationReason\n    }\n    inputFields {\n      ...InputValue\n    }\n    interfaces {\n      ...TypeRef\n    }\n    enumValues(includeDeprecated: true) {\n      name\n      description\n      isDeprecated\n      deprecationReason\n    }\n    possibleTypes {\n      ...TypeRef\n    }\n  }\n\n  fragment InputValue on __InputValue {\n    name\n    description\n    type { ...TypeRef }\n    defaultValue\n  }\n\n  fragment TypeRef on __Type {\n    kind\n    name\n    ofType {\n      kind\n      name\n      ofType {\n        kind\n        name\n        ofType {\n          kind\n          name\n          ofType {\n            kind\n            name\n            ofType {\n              kind\n              name\n              ofType {\n                kind\n                name\n                ofType {\n                  kind\n                  name\n                }\n              }\n            }\n          }\n        }\n      }\n    }\n  }\n"
                }
            """)
        }.response.run {
            testCode(this)
        }
    }

    describe("allows sending a mutation via POST") {
        testResponse(
                call = postJSONRequest {
                    setBody("""
                        {
                            "query": "mutation TestMutation { writeTest { test } }"
                        }
                    """)
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

        testResponse(
                call = postJSONRequest {
                    setBody("""
                        {
                            "query": "query helloWho(${"$"}who: String){ test(who: ${"$"}who) }",
                            "variables": {"who": "Dolly"}
                        }
                        """
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
        testResponse(
                call = postJSONRequest {
                    uri= urlString(
                            "variables" to """
                                {
                                    "who": "Dolly"
                                }
                                """
                    )
                    setBody("""
                        {
                            "query": "query helloWho(${"$"}who: String){ test(who: ${"$"}who) }"
                        }
                        """)
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
        testResponse(
                call = postJSONRequest {
                    setBody("""
                        {
                            "query": "\n query helloYou { test(who: \"You\") }\n query helloWorld { test(who: \"World\") }\n",
                            "operationName": "helloWorld"
                        }
                        """)
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
        testResponse(
                call = postJSONRequest {
                    uri = urlString(Pair("operationName", "helloWorld"))

                    setBody("""
                        {
                            "query": "\n query helloYou { test(who: \"You\") }\n query helloWorld { test(who: \"World\") }\n"
                        }
                        """
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

    describe("supports POST raw text query with GET variable values") {
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
                    setBody("query helloWho(${"$"}who: String){ test(who: ${"$"}who) }")
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

})