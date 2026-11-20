package com.example.remediocerto.util

import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/** Espera uma Task do Firebase dentro de uma coroutine, sem precisar de biblioteca extra. */
suspend fun <T> Task<T>.aguardar(): T = suspendCancellableCoroutine { continuacao ->
    addOnSuccessListener { continuacao.resume(it) }
    addOnFailureListener { continuacao.resumeWithException(it) }
    addOnCanceledListener { continuacao.cancel() }
}
