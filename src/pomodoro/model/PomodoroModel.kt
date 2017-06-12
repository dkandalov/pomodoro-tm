package pomodoro.model

import pomodoro.model.PomodoroState.Mode.*
import pomodoro.model.time.Duration
import pomodoro.model.time.Time
import java.util.*

class PomodoroModel(originalSettings: Settings, val state: PomodoroState) {
    private val listeners = HashMap<Any, Listener>()
    private var settings = originalSettings.copy()
    private var updatedSettings = settings

    init {
        originalSettings.addChangeListener(object : Settings.ChangeListener {
            override fun onChange(newSettings: Settings) {
                updatedSettings = newSettings
            }
        })
        state.progress = progressMax
        state.pomodorosTillLongBreak = settings.longBreakFrequency
    }

    fun onIdeStartup(time: Time) = state.apply {
        if (mode != STOP) {
            val shouldNotContinuePomodoro = Duration.between(lastUpdateTime, time) > settings.timeoutToContinuePomodoro
            if (shouldNotContinuePomodoro) {
                mode = STOP
                lastMode = STOP
                startTime = Time.zero
                progress = Duration.zero
            } else {
                progress = progressSince(time)
            }
        }
    }

    fun onUserSwitchToNextState(time: Time) = state.apply {
        onTimer(time)
        settings = updatedSettings
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
                    settings = updatedSettings
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
                    settings = updatedSettings
                    progress = progressMax
                }
            }
            STOP -> if (lastMode == STOP) {
                return@apply
            }
        }

        listeners.values.forEach { it.onStateChange(this, wasManuallyStopped) }

        lastMode = mode
        lastUpdateTime = time
    }

    val progressMax: Duration
        get() = when (state.mode) {
            RUN -> settings.pomodoroDuration
            BREAK -> if (state.pomodorosTillLongBreak == 0) {
                settings.longBreakDuration
            } else {
                settings.breakDuration
            }
            else -> Duration.zero
        }

    val timeLeft: Duration
        get() = progressMax - state.progress

    fun resetPomodoros() {
        state.pomodorosAmount = 0
    }

    fun addListener(key: Any, listener: Listener) {
        listeners.put(key, listener)
    }

    fun removeListener(key: Any) {
        listeners.remove(key)
    }

    private fun progressSince(time: Time): Duration =
        Duration.between(state.startTime, time).capAt(progressMax)

    interface Listener {
        fun onStateChange(state: PomodoroState, wasManuallyStopped: Boolean)
    }
}
