package ktor.graphql.helpers

import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationResponse
import io.ktor.server.testing.contentType
import org.spekframework.spek2.style.specification.Suite
import kotlin.test.assertEquals
import kotlin.test.expect

fun Suite.testResponse(
        call: TestApplicationCall,
        code: HttpStatusCode = HttpStatusCode.OK,
        json: String,
        contentType: String? = null
) {

    call.response.run {
        testCode(this, code)

        it("has expected json") {
            expect(removeWhitespace(json)) { content }
        }

        contentType?.let { type ->
            testContentType(this, type)
        }
    }
}

fun Suite.testCode(
        response: TestApplicationResponse,
        code: HttpStatusCode = HttpStatusCode.OK
) {
    it("return ${code.value} status code") {
        expect(code) { response.status() }
    }
}

fun Suite.testContentType(
        response: TestApplicationResponse,
        contentType: String
) {
    it("has content type $contentType") {
        assertEquals(contentType, response.contentType().toString())
    }
}