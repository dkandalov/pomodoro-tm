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
import java.util.*


class PomodoroModel(private val settings: Settings, var state: PomodoroState) {
    /**
     * Use WeakHashMap to make it simpler to automatically remove listeners.
     * The most common usage is when there are several IntelliJ windows, UI components subscribe to model and
     * then window is being closed.
     */
    private val listeners = WeakHashMap<Any, Listener>()

    init {
        state.progressMax = updateProgressMax()
        state.progress = state.progressMax
    }

    fun onIdeStartup(now: Long) {
        if (state.type != STOP) {
            val timeSincePomodoroStart = now - state.lastUpdateTime
            val shouldNotContinuePomodoro = timeSincePomodoroStart > settings.timeoutToContinuePomodoro
            if (shouldNotContinuePomodoro) {
                state.type = STOP
                state.lastState = STOP
                state.startTime = -1
                state.progress = 0
            }
        }
    }

    @Synchronized fun onUserSwitchToNextState(time: Long) = state.apply {
        var wasManuallyStopped = false
        when (type) {
            STOP -> {
                type = RUN
                startTime = time
                progressMax = updateProgressMax()
            }
            RUN -> {
                type = STOP
                wasManuallyStopped = true
            }
            BREAK -> {
                type = STOP
                wasManuallyStopped = true
            }
            else -> throw IllegalStateException()
        }
        onTimer(time, wasManuallyStopped)
    }

    @Synchronized fun onTimer(time: Long, wasManuallyStopped: Boolean = false) = state.apply {
        when (type) {
            RUN -> {
                progress = updateProgress(time)
                if (time >= startTime + progressMax) {
                    type = BREAK
                    startTime = time
                    progress = updateProgress(time)
                    progressMax = updateProgressMax()
                    pomodorosAmount++
                }
            }
            BREAK -> {
                updateProgress(time)
                if (time >= startTime + progressMax) {
                    type = STOP
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

    @Synchronized fun getProgressMax(): Int {
        return state.progressMax / progressIntervalMillis
    }

    @Synchronized fun resetPomodoros() {
        state.pomodorosAmount = 0
    }

    @Synchronized fun addUpdateListener(key: Any, listener: Listener) {
        listeners.put(key, listener)
    }

    private fun updateProgress(time: Long): Int {
        return ((time - state.startTime) / progressIntervalMillis).toInt().capAt(getProgressMax())
    }

    private fun updateProgressMax() = when (state.type) {
        RUN -> settings.pomodoroLengthInMillis.toInt()
        BREAK -> settings.breakLengthInMillis.toInt()
        else -> 0
    }

    interface Listener {
        fun onStateChange(state: PomodoroState, wasManuallyStopped: Boolean)
    }

    companion object {
        private const val progressIntervalMillis = 1000
    }
}

private fun Int.capAt(max: Int) = if (this > max) max else this
