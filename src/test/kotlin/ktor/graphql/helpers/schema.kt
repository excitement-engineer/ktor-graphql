package ktor.graphql.helpers

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.SchemaParser
import graphql.schema.DataFetchingEnvironment


val schemaDef = """

type Query {
    test(who: String): String
    testBoolean(value: Boolean): String
    nonNullThrower: String!
    thrower: String
    context: String
    rootValue: String
    slow: String
}

type Mutation {
    writeTest: Query
}
"""

val schema = SchemaParser
        .newParser()
        .schemaString(schemaDef)
        .resolvers(Query(), Mutation())
        .build()
        .makeExecutableSchema()



class Query: GraphQLQueryResolver {
    fun test(who: String?): String = "Hello ${who ?: "World"}"
    fun testBoolean(value: Boolean?): String = "Hello ${value ?: "World"}"
    fun nonNullThrower() {
        throw Exception("Throws!")
    }
    fun thrower() {
        throw Exception("Throws!")
    }
    fun context(env: DataFetchingEnvironment): String {
        return env.getContext()
    }

    fun slow(): String {

        Thread.sleep(100)

        return "hello"
    }

    fun rootValue(env: DataFetchingEnvironment): String = env.getRoot()
}

class Mutation: GraphQLMutationResolver {
    fun writeTest() = Query()
}

