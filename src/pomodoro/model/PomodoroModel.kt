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

import pomodoro.model.PomodoroModel.PomodoroState.*
import java.util.*


class PomodoroModel(private val settings: Settings,
                    private val persistence: PomodoroModelPersistence,
                    now: Long = System.currentTimeMillis()) {

    enum class PomodoroState {
        /**
         * Pomodoro timer was not started or was stopped during pomodoro or break.
         */
        STOP,
        /**
         * Pomodoro in progress.
         */
        RUN,
        /**
         * Pomodoro during break. Can only happen after completing a pomodoro.
         */
        BREAK
    }

    @get:Synchronized lateinit var state: PomodoroState
        private set
    @get:Synchronized lateinit var lastState: PomodoroState
        private set
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

    @Synchronized fun onUserSwitchToNextState() {
        onUserSwitchToNextState(System.currentTimeMillis())
    }

    @Synchronized fun onUserSwitchToNextState(time: Long) {
        when (state) {
            STOP -> {
                state = RUN
                startTime = time
                updateProgressMax()
            }
            RUN -> {
                state = STOP
                wasManuallyStopped = true
            }
            BREAK -> {
                state = STOP
                wasManuallyStopped = true
            }
            else -> throw IllegalStateException()
        }
        onTimer(time)
    }

    @Synchronized fun onTimer(time: Long) {
        when (state) {
            RUN -> {
                updateProgress(time)
                if (time >= startTime + progressMax) {
                    state = BREAK
                    startTime = time
                    updateProgress(time)
                    updateProgressMax()
                    pomodoros++
                }
            }
            BREAK -> {
                updateProgress(time)
                if (time >= startTime + progressMax) {
                    state = STOP
                    wasManuallyStopped = false
                }
            }
            STOP -> if (lastState == STOP) {
                return
            }
        }

        for (listener in listeners.values) {
            listener.invoke()
        }

        if (lastState != state) {
            lastState = state
            saveModelState(time)
        }
        lastState = state
    }

    @Synchronized fun getProgressMax(): Int {
        return progressMax / progressIntervalMillis
    }

    @Synchronized fun resetPomodoros() {
        pomodoros = 0
        persistence.pomodorosAmount = pomodoros
    }

    @Synchronized fun wasManuallyStopped(): Boolean {
        return wasManuallyStopped
    }

    @Synchronized fun addUpdateListener(key: Any, runnable: () -> Unit) {
        listeners.put(key, runnable)
    }

    private fun loadModelState(now: Long) {
        state = persistence.pomodoroState
        lastState = persistence.lastState
        startTime = persistence.startTime
        pomodoros = persistence.pomodorosAmount

        if (persistence.pomodoroState != STOP) {
            val timeSincePomodoroStart = now - persistence.lastUpdateTime
            val shouldNotContinuePomodoro = timeSincePomodoroStart > settings.timeoutToContinuePomodoro
            if (shouldNotContinuePomodoro) {
                state = STOP
                lastState = STOP
                startTime = -1
                saveModelState(now)
            }
        }
    }

    private fun saveModelState(now: Long) {
        persistence.pomodoroState = state
        persistence.lastState = lastState
        persistence.startTime = startTime
        persistence.lastUpdateTime = now
        persistence.pomodorosAmount = pomodoros
    }

    private fun updateProgress(time: Long) {
        progress = ((time - startTime) / progressIntervalMillis).toInt()
        if (progress > getProgressMax()) {
            progress = getProgressMax()
        }
    }

    private fun updateProgressMax() {
        when (state) {
            RUN -> progressMax = settings.pomodoroLengthInMillis.toInt()
            BREAK -> progressMax = settings.breakLengthInMillis.toInt()
        }
    }

    companion object {
        private const val progressIntervalMillis = 1000
    }
}
