package no.uio.ifi.team16.stim.data

import java.time.Instant

data class BarentsWatchToken(val key: String, val validUntil: Instant) {
    fun isValid(): Boolean {
        return Instant.now() > validUntil
    }
}