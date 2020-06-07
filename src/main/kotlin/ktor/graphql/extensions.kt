package ktor.graphql

import graphql.ExecutionInput

fun ExecutionInput.Builder.fromRequest(request: GraphQLRequest) = query(request.query)
        .variables(request.variables ?: emptyMap())
        .operationName(request.operationName)