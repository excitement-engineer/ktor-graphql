package ktor.graphql

import graphql.schema.GraphQLSchema
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.PipelineContext

fun Route.graphQL(
    path: String,
    schema: GraphQLSchema,
    setup: (PipelineContext<Unit, ApplicationCall>.(GraphQLRequest) -> Config)? = null
): Route {

    val graphQLRoute: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit = {
        val requestHandler = RequestHandler(schema, setup)
        requestHandler.doRequest(this)
    }

    return route(path) {
        get(graphQLRoute)
        post(graphQLRoute)
    }
}