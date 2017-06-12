package pomodoro.model.time

import java.time.Instant

data class Time(internal val instant: Instant = Instant.EPOCH) : Comparable<Time> {
    val epochMilli: Long = instant.toEpochMilli()

    constructor(epochMilli: Long) : this(Instant.ofEpochMilli(epochMilli))

    override fun compareTo(other: Time) = instant.compareTo(other.instant)

    operator fun plus(duration: Duration) = Time(instant + duration.delegate)

    companion object {
        val zero = Time(Instant.EPOCH)

        fun now() = Time(Instant.now())
    }
}