package com.paulrod.shelved.ui.search

import androidx.annotation.StringRes
import com.paulrod.shelved.R
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException

enum class SearchFailure(@StringRes val messageResource: Int) {
    NO_CONNECTION(R.string.search_error_connection),
    SERVICE_UNAVAILABLE(R.string.search_error_unavailable),
}

internal fun Throwable.toSearchFailure(): SearchFailure {
    val causes = generateSequence(this) { it.cause }
    return if (causes.any { error ->
            error is UnknownHostException || error is ConnectException || error is NoRouteToHostException
        }
    ) {
        SearchFailure.NO_CONNECTION
    } else {
        // Timeouts and server failures are deliberately indistinguishable to users:
        // both are temporary conditions that Retry can resolve.
        SearchFailure.SERVICE_UNAVAILABLE
    }
}
