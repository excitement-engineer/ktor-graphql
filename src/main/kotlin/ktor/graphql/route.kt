package ktor.graphql

import graphql.schema.GraphQLSchema
import io.ktor.application.ApplicationCall
import io.ktor.pipeline.PipelineContext
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post

fun Route.graphQL(
    schema: GraphQLSchema,
    setup: (PipelineContext<Unit, ApplicationCall>.(GraphQLRequest) -> GraphQLRouteConfig)? = null
) {

    val requestHandler = RequestHandler(schema, setup)

    val graphQLRoute: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit = {
        requestHandler.doRequest(this)
    }

    get(graphQLRoute)
    post(graphQLRoute)
}