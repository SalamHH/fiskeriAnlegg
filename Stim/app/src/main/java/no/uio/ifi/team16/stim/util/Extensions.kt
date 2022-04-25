package no.uio.ifi.team16.stim.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

///////////
// ASYNC //
///////////
/**
 * Asynchronously map on a list. Blocks the caller TODO: I hope, await should do it? but only in scope?
 *
 * let one coroutine handle each entry, then join.
 *
 * TODO: is Dispatchers.IO appropriate? can I get the coroutine-scope of the calling function?
 *
 * @param f function to map with.
 * @return a list corresponding to this.map, but done asynchronously.
 */
suspend fun <T, U> List<T>.mapAsync(f: (T) -> U): List<U> = map { t ->
    CoroutineScope(Dispatchers.IO).async(Dispatchers.IO) {
        f(t)
    }
}.map { deferred ->
    deferred.await() //await the result of each mapping, in total blocks til all are finished
}

/**
 * A chunked version of List.mapAsync. Spawns fewer coroutines.
 *
 * This is not correct. There seems to be a problem in the scoping of the chunk value,
 * which is in the scope of all coroutines and thus shared. This leads to all
 * coroutines working on the same chunk rather than their own.
 */
suspend fun <T, U> List<T>.mapAsync(chunks: Int, f: (T) -> U): List<U> =
    chunked(chunks) { chunk -> //list of rows
        //Log.d(TAG, "processing CHUNK[${chunk.size}]: ${chunk}")
        CoroutineScope(Dispatchers.IO).async(Dispatchers.IO) {
            //val mc = chunk
            chunk.map { t -> //for each row apply
                //Log.d(TAG, "processing ENTRY ${t} in CHUNK: ${chunk}")
                f(t)
            }
        }
    }.map { deferred -> //list of rows deferred
        val v =
            deferred.await() //await the result of each mapping, in total blocks til all are finished
        //Log.d(TAG, "got an array of size " + v.size)
        //Log.d(TAG, "$v")
        v
    }.filter {
        isNotEmpty()
    }.flatten()