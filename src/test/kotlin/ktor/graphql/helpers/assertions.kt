package ktor.graphql.helpers

import kotlin.test.assertFalse
import kotlin.test.assertTrue

fun assertContains(actual: String, containing: String) {
    val containsMessage =
            """
        Expected:
        $actual

        To contain:
        $containing
        """

    assertTrue(actual.contains(containing), containsMessage)
}

fun assertDoesntContains(actual: String, containing: String) {
    val containsMessage =
            """
        Expected:
        $actual

        Not to contain:
        $containing
        """

    assertFalse(actual.contains(containing), containsMessage)
}