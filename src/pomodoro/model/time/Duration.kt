package pomodoro.model.time

import java.time.Duration as JavaDuration

/**
 * The main reason to wrap java Duration is that it doesn't have default constructor
 * and IJ persistence needs it when creating transient fields.
 * Using wrapper class might be useful to see exactly which part of Duration API is used.
 */
data class Duration(internal val delegate: JavaDuration = JavaDuration.ZERO) : Comparable<Duration> {
    val seconds: Long = delegate.seconds
    val minutes: Long = delegate.toMinutes()

    constructor(minutes: Int) : this(minutes.toLong())
    constructor(minutes: Long) : this(JavaDuration.ofMinutes(minutes))

    override fun compareTo(other: Duration) = delegate.compareTo(other.delegate)

    operator fun minus(that: Duration): Duration = Duration(delegate - that.delegate)

    fun capAt(max: Duration) = if (this > max) max else this

    companion object {
        val zero = Duration(0)

        fun between(start: Time, end: Time) = Duration(JavaDuration.between(start.instant, end.instant))

        fun ofDays(days: Long) = Duration(JavaDuration.ofDays(days))
    }
}

val Number.minutes: Duration
    get() = Duration(minutes = toInt())

val Number.days: Duration
    get() = Duration.ofDays(days = toLong())