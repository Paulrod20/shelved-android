package com.paulrod.shelved.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

internal class OrderedWriteQueue<T>(
    scope: CoroutineScope,
    private val writer: suspend (T) -> Unit,
    private val onError: (Throwable) -> Unit,
) {
    private val writes = Channel<T>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (value in writes) {
                try {
                    writer(value)
                } catch (error: Throwable) {
                    if (error is CancellationException) throw error
                    onError(error)
                }
            }
        }
    }

    fun enqueue(value: T) {
        check(writes.trySend(value).isSuccess) { "The persistence queue is unavailable." }
    }
}
