package ktor.graphql.helpers

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.util.pipeline.PipelineContext
import ktor.graphql.GraphQLRequest
import ktor.graphql.Config
import ktor.graphql.graphQL

fun TestApplicationEngine.testGraphQLServer(
        setup: (PipelineContext<Unit, ApplicationCall>.(GraphQLRequest) -> Config)? = null
) = application.routing {
    graphQL(urlString(), schema, setup)
}

fun Application.testGraphQLRoute() {
    routing {
        graphQL(urlString(), schema)
    }
}