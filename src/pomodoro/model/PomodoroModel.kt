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


class PomodoroModel(private val settings: Settings,
                    private var state: PomodoroState,
                    now: Long = System.currentTimeMillis()) {

    @get:Synchronized lateinit var stateType: PomodoroState.Type private set
    @get:Synchronized lateinit var lastStateType: PomodoroState.Type private set
    private var startTime: Long = 0
    private var progressMax: Int = 0
    @get:Synchronized var progress: Int = 0
        private set
    @get:Synchronized var pomodoros: Int = 0
        private set
    private var wasManuallyStopped: Boolean = false
    /**
     * It's a WeakHashMap to make it simpler to automatically remove listeners.
     * The most common usage is when there are several IntelliJ windows, UI components subscribe to model and
     * then window is being closed.
     */
    private val listeners = WeakHashMap<Any, () -> Unit>()

    init {
        loadModelState(now)
        updateProgressMax()
        progress = progressMax
    }

    @Synchronized fun onUserSwitchToNextState(time: Long) {
        when (stateType) {
            STOP -> {
                stateType = RUN
                startTime = time
                updateProgressMax()
            }
            RUN -> {
                stateType = STOP
                wasManuallyStopped = true
            }
            BREAK -> {
                stateType = STOP
                wasManuallyStopped = true
            }
            else -> throw IllegalStateException()
        }
        onTimer(time)
    }

    @Synchronized fun onTimer(time: Long) {
        when (stateType) {
            RUN -> {
                updateProgress(time)
                if (time >= startTime + progressMax) {
                    stateType = BREAK
                    startTime = time
                    updateProgress(time)
                    updateProgressMax()
                    pomodoros++
                }
            }
            BREAK -> {
                updateProgress(time)
                if (time >= startTime + progressMax) {
                    stateType = STOP
                    wasManuallyStopped = false
                }
            }
            STOP -> if (lastStateType == STOP) {
                return
            }
        }

        for (listener in listeners.values) {
            listener.invoke()
        }

        if (lastStateType != stateType) {
            lastStateType = stateType
            saveModelState(time)
        }
        lastStateType = stateType
    }

    @Synchronized fun getProgressMax(): Int {
        return progressMax / progressIntervalMillis
    }

    @Synchronized fun resetPomodoros() {
        pomodoros = 0
        state.pomodorosAmount = pomodoros
    }

    @Synchronized fun wasManuallyStopped(): Boolean {
        return wasManuallyStopped
    }

    @Synchronized fun addUpdateListener(key: Any, runnable: () -> Unit) {
        listeners.put(key, runnable)
    }

    private fun loadModelState(now: Long) {
        stateType = state.type
        lastStateType = state.lastState
        startTime = state.startTime
        pomodoros = state.pomodorosAmount

        if (state.type != STOP) {
            val timeSincePomodoroStart = now - state.lastUpdateTime
            val shouldNotContinuePomodoro = timeSincePomodoroStart > settings.timeoutToContinuePomodoro
            if (shouldNotContinuePomodoro) {
                stateType = STOP
                lastStateType = STOP
                startTime = -1
                saveModelState(now)
            }
        }
    }

    private fun saveModelState(now: Long) {
        state.type = stateType
        state.lastState = lastStateType
        state.startTime = startTime
        state.lastUpdateTime = now
        state.pomodorosAmount = pomodoros
    }

    private fun updateProgress(time: Long) {
        progress = ((time - startTime) / progressIntervalMillis).toInt()
        if (progress > getProgressMax()) {
            progress = getProgressMax()
        }
    }

    private fun updateProgressMax() {
        when (stateType) {
            RUN -> progressMax = settings.pomodoroLengthInMillis.toInt()
            BREAK -> progressMax = settings.breakLengthInMillis.toInt()
        }
    }

    companion object {
        private const val progressIntervalMillis = 1000
    }
}
