package pomodoro.model

import pomodoro.model.PomodoroState.Mode.*
import pomodoro.model.time.Duration
import pomodoro.model.time.Time
import java.util.*

// TODO when settings changes think about how change is applied
class PomodoroModel(private val settings: Settings, var state: PomodoroState) {
    /**
     * Use WeakHashMap to make it simpler to automatically remove listeners.
     * The most common usage is when there are several IntelliJ windows, UI components subscribe to model and
     * then window is being closed.
     */
    private val listeners = WeakHashMap<Any, Listener>()

    init {
        state.progress = progressMax
        state.pomodorosTillLongBreak = settings.longBreakFrequency
    }

    fun onIdeStartup(time: Time) = state.apply {
        if (mode != STOP) {
            val timeSincePomodoroStart = Duration.between(lastUpdateTime, time)
            val shouldNotContinuePomodoro = timeSincePomodoroStart > settings.timeoutToContinuePomodoro
            if (shouldNotContinuePomodoro) {
                mode = STOP
                lastMode = STOP
                startTime = Time.ZERO
                progress = Duration.ZERO
            } else {
                progress = progressSince(time)
            }
        }
    }

    fun onUserSwitchToNextState(time: Time) = state.apply {
        onTimer(time)
        var wasManuallyStopped = false
        when (mode) {
            RUN -> {
                mode = STOP
                progress = progressMax
                wasManuallyStopped = true
                if (pomodorosTillLongBreak == 0) {
                    pomodorosTillLongBreak = settings.longBreakFrequency
                }
            }
            BREAK -> {
                mode = STOP
                progress = progressMax
                wasManuallyStopped = true
            }
            STOP -> {
                mode = RUN
                startTime = time
            }
        }
        onTimer(time, wasManuallyStopped)
    }

    fun onTimer(time: Time, wasManuallyStopped: Boolean = false) = state.apply {
        when (mode) {
            RUN -> {
                progress = progressSince(time)
                if (time >= startTime + progressMax) {
                    mode = BREAK
                    startTime = time
                    progress = progressSince(time)
                    pomodorosAmount++
                    pomodorosTillLongBreak--
                }
            }
            BREAK -> {
                progress = progressSince(time)
                if (time >= startTime + progressMax) {
                    mode = STOP
                    progress = progressMax
                }
            }
            STOP -> if (lastMode == STOP) {
                return@apply
            }
        }

        listeners.values.forEach { it.onStateChange(this, wasManuallyStopped) }

        lastMode = mode
    }

    val progressMax: Duration
        get() = when (state.mode) {
            RUN -> settings.pomodoroDuration
            BREAK -> if (state.pomodorosTillLongBreak == 0) {
                settings.longBreakDuration
            } else {
                settings.breakDuration
            }
            else -> Duration.ZERO
        }

    val timeLeft: Duration
        get() = progressMax - state.progress

    fun resetPomodoros() {
        state.pomodorosAmount = 0
    }

    fun addUpdateListener(key: Any, listener: Listener) {
        listeners.put(key, listener)
    }

    private fun progressSince(time: Time): Duration {
        return Duration.between(state.startTime, time).capAt(progressMax)
    }

    interface Listener {
        fun onStateChange(state: PomodoroState, wasManuallyStopped: Boolean)
    }
}
