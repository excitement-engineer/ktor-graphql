package ktor.graphql

import io.ktor.server.testing.setBody
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object Post : Spek({

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

        postJSONRequest {
            setBody("""
            {
                "query": "mutation TestMutation { writeTest { test } }"
            }
            """)
        }.response.run {
            
        }

    }

})