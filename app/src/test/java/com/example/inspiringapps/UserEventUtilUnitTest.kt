package com.example.inspiringapps

import com.example.inspiringapps.model.Sequence
import com.example.inspiringapps.testutils.UserEventUtilTestUtil
import com.example.inspiringapps.util.UserEventUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserEventUtilUnitTest {

    /*
    we'll simulate three users visiting the site with overlapping requests
     */
    @Test
    fun testLogFileParsing() {
        val parseResults : Map<Sequence, Int> = UserEventUtil.parseLogFile(UserEventUtilTestUtil.getUserEventList())

        assertValidTestResults(parseResults)
    }

    /*
    Now we'll simulate invalid endpoints to make sure they don't sneak in
     */
    @Test
    fun testInvalidEndpointsFromUser() {
        val parseResultsWithInvalidEndpoints : Map<Sequence, Int> = UserEventUtil.parseLogFile(UserEventUtilTestUtil.getUserEventListWithInvalids())

        assertInvalidTestResults(parseResultsWithInvalidEndpoints)

    }

    private fun assertValidTestResults(validResults: Map<Sequence, Int>) {
        assertEquals(2, validResults[Sequence("/a/", "/b/", "/c/")])
        assertEquals(2, validResults[Sequence("/a/", "/b/", "/d/")])
        assertEquals(2, validResults[Sequence("/b/", "/c/", "/d/")])
        assertEquals(2, validResults[Sequence("/c/", "/d/", "/a/")])
        assertEquals(1, validResults[Sequence("/d/", "/a/", "/a/")])
        assertEquals(1, validResults[Sequence("/d/", "/a/", "/b/")])
        assertEquals(1, validResults[Sequence("/b/", "/a/", "/b/")])
        assertEquals(1, validResults[Sequence("/a/", "/b/", "/a/")])
    }

    private fun assertInvalidTestResults(validResults: Map<Sequence, Int>) {
        assertEquals(2, validResults[Sequence("/b/", "/c/", "/d/")])
        assertEquals(2, validResults[Sequence("/c/", "/d/", "/a/")])
        assertEquals(1, validResults[Sequence("/a/", "/a/", "/b/")])
        assertEquals(1, validResults[Sequence("/a/", "/b/", "/d/")])

        assertNull(validResults[Sequence("/X/", "/Y/", "/Z/")])
    }
}