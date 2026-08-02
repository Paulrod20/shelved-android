package com.paulrod.shelved.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderedWriteQueueTest {
    @Test
    fun writesFinishInTheSameOrderTheyWereEnqueued() = runTest {
        val events = mutableListOf<String>()
        val queueScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        val queue = OrderedWriteQueue<Int>(
            scope = queueScope,
            writer = { value ->
                events += "start$value"
                delay(if (value == 1) 100 else 1)
                events += "end$value"
            },
            onError = { throw it },
        )

        queue.enqueue(1)
        queue.enqueue(2)
        queue.enqueue(3)
        advanceUntilIdle()
        queueScope.cancel()

        assertEquals(
            listOf("start1", "end1", "start2", "end2", "start3", "end3"),
            events,
        )
    }

    @Test
    fun failedWriteDoesNotStopLaterWrites() = runTest {
        val completed = mutableListOf<Int>()
        val errors = mutableListOf<String>()
        val queueScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        val queue = OrderedWriteQueue<Int>(
            scope = queueScope,
            writer = { value ->
                if (value == 1) error("first failed")
                completed += value
            },
            onError = { errors += it.message.orEmpty() },
        )

        queue.enqueue(1)
        queue.enqueue(2)
        advanceUntilIdle()
        queueScope.cancel()

        assertEquals(listOf("first failed"), errors)
        assertEquals(listOf(2), completed)
    }
}
