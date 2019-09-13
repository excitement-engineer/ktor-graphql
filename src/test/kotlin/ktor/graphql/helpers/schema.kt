package ktor.graphql.helpers

import graphql.Scalars.GraphQLBoolean
import graphql.Scalars.GraphQLString
import graphql.schema.GraphQLNonNull.nonNull
import graphql.schema.GraphQLObjectType.newObject
import graphql.schema.GraphQLSchema


val queryType = newObject()
        .name("Query")
        .Field {
            name("test")
            Argument {
                name("who")
                type(GraphQLString)
            }
            type(GraphQLString)
            dataFetcher { env ->

                val who = env.getArgument<String>("who")

                "Hello ${who ?: "World"}"
            }
        }
        .Field {
            name("testBoolean")
            Argument {
                name("value")
                type(GraphQLBoolean)
            }
            type(GraphQLString)
            dataFetcher { env ->

                val value = env.getArgument<Boolean>("value")

                "Hello ${value ?: "World"}"
            }
        }
        .Field {

            name("nonNullThrower")
            type(nonNull(GraphQLString))
            dataFetcher {
                throw Exception("Throws!")
            }
        }
        .Field {
            name("thrower")
            type(GraphQLString)
            dataFetcher {
                throw Exception("Throws!")
            }
        }
        .Field {
            name("context")
            type(GraphQLString)
            dataFetcher { it.getContext() }
        }
        .Field {

            name("slow")
            type(GraphQLString)
            dataFetcher {
                Thread.sleep(100)

                "hello"
            }
        }
        .Field {
            name("rootValue")
            type(GraphQLString)
            dataFetcher { it.getRoot() }
        }
        .build()

val mutationType = newObject()
        .name("Mutation")
        .Field {
            name("writeTest")
            type(queryType)
            dataFetcher { object {} }
        }
        .build()


val schema = GraphQLSchema(queryType, mutationType, emptySet())

