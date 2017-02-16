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

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Class for persisting pomodoro state.
 * It was not implemented as part of [Settings] class
 * because instances of this class cannot be directly changed by user.

 * Thread-safe because it's accessed from [ControlThread] and
 * IntelliJ platform thread which persists it.
 */
@State(name = "PomodoroState", storages = arrayOf(Storage(id = "other", file = "\$APP_CONFIG$/pomodoro.state.xml")))
class PomodoroModelState : PersistentStateComponent<PomodoroModelState> {
    @get:Synchronized @set:Synchronized var pomodoroState: PomodoroModel.PomodoroState
    @get:Synchronized @set:Synchronized var lastState: PomodoroModel.PomodoroState
    @get:Synchronized @set:Synchronized var startTime: Long
    @get:Synchronized @set:Synchronized var lastUpdateTime: Long
    @get:Synchronized @set:Synchronized var pomodorosAmount: Int

    constructor() {
        pomodoroState = PomodoroModel.PomodoroState.STOP
        lastState = PomodoroModel.PomodoroState.STOP
        startTime = 0
        lastUpdateTime = 0
        pomodorosAmount = 0
    }

    constructor(pomodoroState: PomodoroModel.PomodoroState, lastState: PomodoroModel.PomodoroState, startTime: Long, lastUpdateTime: Long) {
        this.pomodoroState = pomodoroState
        this.lastState = lastState
        this.startTime = startTime
        this.lastUpdateTime = lastUpdateTime
        this.pomodorosAmount = 0
    }

    override fun getState() = this

    @Synchronized override fun loadState(state: PomodoroModelState) = XmlSerializerUtil.copyBean(state, this)
}
