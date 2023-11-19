package pomodoro.model

import pomodoro.model.PomodoroSnapshot.Period.*
import pomodoro.model.PomodoroSnapshot.Status.Completed
import pomodoro.model.PomodoroSnapshot.Status.Failed
import pomodoro.model.time.Duration
import pomodoro.model.time.Time
import pomodoro.model.time.days

data class PomodoroSnapshot(val startTime: Time = Time.zero, val endTime: Time = Time.zero, val status: Status = Completed) {
    enum class Status {
        Completed,
        Failed
    }
    enum class Period(val duration: Duration) {
        Today(1.days),
        PastWeek(7.days),
        Past28Days(28.days),
        OutOfRange(Duration.zero)
    }

    private fun isInPeriod(referenceTime: Time, period: Period): Boolean {
        val startOfDay = referenceTime.atStartOfDay()
        return when (period) {
            Today -> this.startTime in startOfDay..referenceTime
            PastWeek -> this.startTime in startOfDay - PastWeek.duration .. referenceTime
            Past28Days -> this.startTime in startOfDay - Past28Days.duration .. referenceTime
            OutOfRange -> false
        }
    }

    companion object {

        fun completed(startTime: Time, endTime: Time) = PomodoroSnapshot(startTime, endTime, Completed)

        fun failed(startTime: Time, endTime: Time) = PomodoroSnapshot(startTime, endTime, Failed)

        fun statisticsAt(time: Time, history: List<PomodoroSnapshot>): PomodoroStatistics {
            return PomodoroStatistics(today = pomodoroCountsInPeriod(history, time, Today),
                    week = pomodoroCountsInPeriod(history, time, PastWeek),
                    month = pomodoroCountsInPeriod(history, time, Past28Days))
        }

        private fun pomodoroCountsInPeriod(history: List<PomodoroSnapshot>, time: Time, period: Period): PomodoroCounts {
            val countsByStatus = history.filter { s -> s.isInPeriod(time, period) }.groupingBy { s -> s.status }.eachCount()
            return PomodoroCounts.from(countsByStatus)
        }
    }
}

data class PomodoroStatistics(val today: PomodoroCounts, val week: PomodoroCounts, val month: PomodoroCounts)

data class PomodoroCounts(val completed: Int = 0, val failed: Int = 0) {
    companion object {
        fun from(counts: Map<PomodoroSnapshot.Status, Int>) = PomodoroCounts(
                completed = counts.getOrDefault(Completed, 0),
                failed = counts.getOrDefault(Failed, 0))
    }
}
