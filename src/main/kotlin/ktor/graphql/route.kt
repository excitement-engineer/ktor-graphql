package ktor.graphql

import graphql.schema.GraphQLSchema
import io.ktor.application.ApplicationCall
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
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