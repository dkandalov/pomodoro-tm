/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pomodoro.model

import pomodoro.model.PomodoroState.Type.*
import pomodoro.model.time.Duration
import pomodoro.model.time.Time
import java.util.*


class PomodoroModel(private val settings: Settings, var state: PomodoroState) {
    /**
     * Use WeakHashMap to make it simpler to automatically remove listeners.
     * The most common usage is when there are several IntelliJ windows, UI components subscribe to model and
     * then window is being closed.
     */
    private val listeners = WeakHashMap<Any, Listener>()

    init {
        state.progress = progressMax
    }

    fun onIdeStartup(time: Time) = state.apply {
        if (type != STOP) {
            val timeSincePomodoroStart = Duration.between(lastUpdateTime, time)
            val shouldNotContinuePomodoro = timeSincePomodoroStart > settings.timeoutToContinuePomodoro
            if (shouldNotContinuePomodoro) {
                type = STOP
                lastState = STOP
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
        when (type) {
            STOP -> {
                type = RUN
                startTime = time
            }
            RUN -> {
                type = STOP
                progress = progressMax
                wasManuallyStopped = true
            }
            BREAK -> {
                type = STOP
                progress = progressMax
                wasManuallyStopped = true
            }
            else -> throw IllegalStateException()
        }
        onTimer(time, wasManuallyStopped)
    }

    fun onTimer(time: Time, wasManuallyStopped: Boolean = false) = state.apply {
        when (type) {
            RUN -> {
                progress = progressSince(time)
                if (time >= startTime + progressMax) {
                    type = BREAK
                    startTime = time
                    progress = progressSince(time)
                    pomodorosAmount++
                }
            }
            BREAK -> {
                progress = progressSince(time)
                if (time >= startTime + progressMax) {
                    type = STOP
                    progress = progressMax
                }
            }
            STOP -> if (lastState == STOP) {
                return@apply
            }
        }

        for (listener in listeners.values) {
            listener.onStateChange(state, wasManuallyStopped)
        }

        if (lastState != type) {
            lastState = type
        }
        lastState = type
    }

    val progressMax: Duration
        get() = when (state.type) {
            RUN -> settings.pomodoroDuration
            BREAK -> settings.breakDuration
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
