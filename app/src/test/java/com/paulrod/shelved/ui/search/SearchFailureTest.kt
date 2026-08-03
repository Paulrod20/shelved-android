package com.paulrod.shelved.ui.search

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchFailureTest {
    @Test
    fun missingNetworkMapsToConnectionMessage() {
        assertEquals(SearchFailure.NO_CONNECTION, UnknownHostException().toSearchFailure())
    }

    @Test
    fun timeoutMapsToTemporaryServiceMessage() {
        assertEquals(SearchFailure.SERVICE_UNAVAILABLE, SocketTimeoutException().toSearchFailure())
    }

    @Test
    fun wrappedNetworkErrorsAreRecognized() {
        val wrapped = IllegalStateException("request failed", UnknownHostException())

        assertEquals(SearchFailure.NO_CONNECTION, wrapped.toSearchFailure())
    }
}
