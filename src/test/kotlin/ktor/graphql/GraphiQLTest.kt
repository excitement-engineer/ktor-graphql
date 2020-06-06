package ktor.graphql

import ktor.graphql.explorer.renderGraphiQL
import ktor.graphql.helpers.assertContains
import ktor.graphql.helpers.assertDoesntContains
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object GraphiQLTest : Spek({

    fun render(request: GraphQLRequest) = renderGraphiQL(
            data = mapOf("data" to mapOf("test" to "Hello World")),
            request = request
    )

    describe("GraphiQL explorer") {

        val request = GraphQLRequest(
                query = "hello",
                variables = mapOf(
                        "test" to "hello"
                ),
                operationName = "operationName"
        )


        val content = render(request)

        it("contains a pre-run response") {
            assertContains(content, "response: JSON.stringify({\"data\":{\"test\":\"Hello World\"}}, null, 2)")
        }

        it("contains the operation name") {
            assertContains(content, "operationName: \"operationName\"")
        }

        it("contains the variables") {
            assertContains(content, "variables: JSON.stringify({\"test\":\"hello\"}, null, 2)")
        }
    }

    describe("escapes HTML within GraphiQL") {

        it("escapes HTML in queries within GraphiQL") {
            val content = render(GraphQLRequest(
                    query = "</script><script>alert(1)</script>"
            ))
            assertDoesntContains(content, "</script><script>alert(1)</script>")
        }



        it("escapes HTML in variables within GraphiQL") {
            val content = render(GraphQLRequest(
                    query = "hello",
                    variables = mapOf(
                            "who" to "</script><script>alert(1)</script>"
                    )
            ))
            assertDoesntContains(content, "</script><script>alert(1)</script>")
        }
    }

    describe("GraphiQL accepts an empty query") {

        it("contains an undefined response") {
            val content = renderGraphiQL(null, GraphQLRequest())

            assertContains(content, "response: undefined")
        }
    }
})