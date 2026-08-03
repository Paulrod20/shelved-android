package com.paulrod.shelved.data

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CloudflareGameCatalogTest {
    @Test
    fun searchMapsGamesAndSendsAppCheckToken() = runTest {
        var receivedToken: String? = null
        val server = server { exchange ->
            receivedToken = exchange.requestHeaders.getFirst("X-Firebase-AppCheck")
            exchange.respond(200, SEARCH_RESPONSE)
        }
        try {
            val catalog = catalog(server) { "debug-token" }

            val games = catalog.search("Mario Kart")

            assertEquals("debug-token", receivedToken)
            assertEquals(listOf("igdb:1"), games.map { it.id })
            assertEquals(listOf("Mario Kart"), games.map { it.name })
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun unauthorizedResponseRefreshesAppCheckTokenOnce() = runTest {
        val requestCount = AtomicInteger()
        val refreshValues = mutableListOf<Boolean>()
        val server = server { exchange ->
            if (requestCount.incrementAndGet() == 1) {
                exchange.respond(401, "{\"error\":\"Unauthorized.\"}")
            } else {
                exchange.respond(200, SEARCH_RESPONSE)
            }
        }
        try {
            val catalog = catalog(server) { forceRefresh ->
                refreshValues += forceRefresh
                if (forceRefresh) "fresh-token" else "cached-token"
            }

            assertEquals(1, catalog.search("Mario Kart").size)
            assertEquals(listOf(false, true), refreshValues)
            assertEquals(2, requestCount.get())
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun workerErrorMessageIsPreserved() = runTest {
        val server = server { it.respond(429, "{\"error\":\"Too many requests.\"}") }
        try {
            val error = runCatching { catalog(server) { "token" }.search("Mario") }.exceptionOrNull()

            assertEquals(429, (error as GameCatalogException).status)
            assertEquals("Too many requests.", error.message)
        } finally {
            server.stop(0)
        }
    }

    private fun catalog(
        server: HttpServer,
        token: suspend (Boolean) -> String,
    ) = CloudflareGameCatalog(
        baseUrl = "http://127.0.0.1:${server.address.port}",
        appCheckToken = token,
        ioDispatcher = Dispatchers.Unconfined,
    )

    private fun server(handler: (HttpExchange) -> Unit): HttpServer =
        HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/") { exchange -> handler(exchange) }
            start()
        }

    private fun HttpExchange.respond(status: Int, body: String) {
        val bytes = body.toByteArray()
        responseHeaders.set("Content-Type", "application/json")
        sendResponseHeaders(status, bytes.size.toLong())
        responseBody.use { it.write(bytes) }
    }

    private companion object {
        const val SEARCH_RESPONSE = """
            {"games":[{"id":"igdb:1","name":"Mario Kart","coverImageUrl":null,"released":null,"playtime":null,"platforms":["Switch"],"description":null}]}
        """
    }
}
