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
        originalSettings.addChangeListener(object: Settings.ChangeListener {
            override fun onChange(newSettings: Settings) {
                updatedSettings = newSettings
            }
        })
        state.progress = progressMax
        state.pomodorosTillLongBreak = settings.longBreakFrequency
    }

    fun onIdeStartup(time: Time) = state.apply {
        if (mode != Stop) {
            val shouldNotContinuePomodoro = Duration.between(lastUpdateTime, time) > settings.timeoutToContinuePomodoro
            if (shouldNotContinuePomodoro) {
                mode = Stop
                lastMode = Stop
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
            Run   -> {
                mode = Stop
                progress = progressMax
                wasManuallyStopped = true
                if (pomodorosTillLongBreak == 0) {
                    pomodorosTillLongBreak = settings.longBreakFrequency
                }
            }
            Break -> {
                mode = Stop
                progress = progressMax
                wasManuallyStopped = true
            }
            Stop  -> {
                mode = Run
                startTime = time
            }
        }
        onTimer(time, wasManuallyStopped)
    }

    fun onTimer(time: Time, wasManuallyStopped: Boolean = false) = state.apply {
        when (mode) {
            Run   -> {
                progress = progressSince(time)
                if (time >= startTime + progressMax) {
                    mode = Break
                    settings = updatedSettings
                    startTime = time
                    progress = progressSince(time)
                    pomodorosAmount++
                    pomodorosTillLongBreak--
                }
            }
            Break -> {
                progress = progressSince(time)
                if (time >= startTime + progressMax) {
                    settings = updatedSettings
                    if (settings.startNewPomodoroAfterBreak) {
                        mode = Run
                        startTime = time
                    } else {
                        mode = Stop
                    }
                    progress = progressMax
                }
            }
            Stop  -> if (lastMode == Stop) {
                return@apply
            }
        }

        listeners.values.forEach { it.onStateChange(this, wasManuallyStopped) }

        lastMode = mode
        lastUpdateTime = time
    }

    val progressMax: Duration
        get() = when (state.mode) {
            Run   -> settings.pomodoroDuration
            Break ->
                if (state.pomodorosTillLongBreak == 0) {
                    settings.longBreakDuration
                } else {
                    settings.breakDuration
                }
            else  -> Duration.zero
        }

    val timeLeft: Duration
        get() = progressMax - state.progress

    fun resetPomodoros() {
        state.pomodorosAmount = 0
    }

    fun addListener(key: Any, listener: Listener) {
        listeners[key] = listener
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
