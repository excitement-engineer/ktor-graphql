# Ktor Graphql

Easily serve GraphQL over http together with Ktor.

## Installation: 

This project is hosted on jCenter.

Add dependency:

**Maven:**

```
<dependency>
    <groupId>com.github.excitement-engineer</groupId>
    <artifactId>ktor-graphql</artifactId>
    <version>${version}</version>
</dependency>
```

**Gradle:** 

```
compile 'com.github.excitement-engineer:ktor-graphql:${version}'
```

## Setup

Add the `graphQL` route inside of Ktor's `routing` feature:

```
val server = embeddedServer(Netty, port = 8080) {
    routing {
        graphQL("/graphql", schema) { request ->
            Config(showExplorer = true)
        }
    }
}

server.start(wait = true)
``` 

The `graphQL` route has two required parameters:
 
- The path associated with the graphQL endpoint.
- A graphQL schema (see [GraphQL-Java](https://github.com/graphql-java/graphql-java)).

Optionally, various options can be passed in a `Config` in the trailing callback. The callback is invoked every time that a request
is executed against the server.

## Configuration

The `Config` class accepts the following parameters:

* `showExplorer`: If true, then a [GraphQL-playground](https://github.com/prisma-labs/graphql-playground) is presented when the graphQL endpoint is loaded in the browser. This is useful for testing your application during development.
* `formatError`: An optional function that it is used to format any errors that occur in completing a GraphQL operation. If not provided, GraphQL's default spec-compliant [`GraphQLError.toSpecification()`](https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/GraphQLError.java) is used.
* `executeRequest`: A custom function for executing queries against your schema. 
* `renderExplorer`: A function to render a custom graphql explorer. The default is Graphql-Playground however this library also contains a way for rendering 
GraphiQL (see `ktor.graphql.explorer.graphiQL`).


# HTTP usage

`ktor-graphql` will accept requests with the parameters:

* `query`: A string GraphQL document to be executed.
* `variables`: The runtime values to use for any GraphQL query variables as a JSON object.
* `operationName`: If the provided query contains multiple named operations, this specifies which operation should be executed. If not provided, a 400 error will be returned if the query contains multiple named operations.
* `raw`: If the `showExplorer` option is enabled and the raw parameter is provided raw JSON will always be returned instead of the GraphQL explorer even when loaded from a browser.

GraphQL will first look for each parameter in the URL's query-string:

```
/graphql?query=query+getUser($id:ID){user(id:$id){name}}&variables={"id":"4"}
```

If not found in the query-string, it will look in the POST request body.

`ktor-graphql` will interpret the POST body depending on the provided *Content-Type* header.

* `application/json`: the POST body will be parsed as a JSON object of parameters.
* `application/graphql`: the POST body will be parsed as GraphQL query string, which provides the query parameter.

## Guides

### Passing a root value or context

In order to pass a context when executing a query against the schema we can use the `executeRequest` option of the `Config`.
 
```

val graphQL = GraphQL.newGraphQL(schema).build()

val server = embeddedServer(Netty, port = 8080) {
    routing {
        graphQL("/graphql", schema) { request ->
            Config(
                executeRequest = {
                    val input = ExecutionInput.newExecutionInput()
                            .fromRequest(request)
                            .context(yourContent)
                            .root(yourRootValue)
                            .build()

                    graphQL.execute(input)
                }
            )
        }
    }
}

server.start(wait = true)
``` 

You can also pass a [dataLoader](https://www.graphql-java.com/documentation/v12/batching/) instance into `ExecutionInput`.

### Masking exceptions

An unexpected exception may occur in your application that you may not want to expose to consumers of your API because 
they may contain cryptic errors or may expose server-side code. For example, if the query `{ user(id: "1") { name } }` is executed then
the server will respond with `Could not cast db.user to User`. A more constructive error could be 
`Internal server error. Please report this error to errors@domain.com`

In order to do this we introduce an Exception subclass `ClientException`. This exception should be thrown whenever
an error occurs that the user of the API should be notified about. An use case for `ClientException` is an error stating that you need to be authenticated
to perform the operation.

```
class ClientException(message: String): Exception(message)
```

Next, we customize the error behavior in the `formatError` option by customizing the error `message`
depending on whether the original exception is a `ClientException` or not.

```
graphQL("/graphql", schema) {
    Config(
        formatError = { error ->
            val clientMessage = if (error is ExceptionWhileDataFetching) {

                val exception = error.exception

                val formattedMessage = if (exception is ClientException) {
                    exception.message
                } else {
                    "Internal server error"
                }

                formattedMessage
            } else {
                error.message
            }

            val result = error.toSpecification()
            result["message"] = clientMessage

            result
        }
    )
}
```

The [example repo](https://github.com/excitement-engineer/ktor-graphql-example) provides an example on how to implement error masking. 

## Example

Refer to the [ktor-graphql-example repo](https://github.com/excitement-engineer/ktor-graphql-example) for an example
on how to use `ktor-graphql`. It provides an example on how to implement:

* Authentication of a user using an access token and then passing the authenticated user to GraphQL's context.
* Masking internal server errors using the `formatError` function.
* Writing tests against you graphQL endpoint.

## Credit

This library is based on the [express-graphql](https://github.com/graphql/express-graphql) library. 

Part of the documentation, API and test cases in this library are based on express-graphql, credit goes to the authors of this library.

