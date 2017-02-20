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
import pomodoro.model.PomodoroModel.PomodoroState

/**
 * Class for persisting pomodoro state.
 * It was not implemented as part of [Settings] class
 * because instances of this class cannot be directly changed by user.

 * Thread-safe because it's accessed from [ControlThread] and
 * IntelliJ platform thread which persists it.
 */
@State(name = "PomodoroState", storages = arrayOf(Storage(id = "other", file = "\$APP_CONFIG$/pomodoro.state.xml")))
class PomodoroModelPersistence : PersistentStateComponent<PomodoroModelPersistence> {
    @get:Synchronized @set:Synchronized var pomodoroState = PomodoroState.STOP
    @get:Synchronized @set:Synchronized var lastState = PomodoroState.STOP
    @get:Synchronized @set:Synchronized var startTime = 0L
    @get:Synchronized @set:Synchronized var lastUpdateTime = 0L
    @get:Synchronized @set:Synchronized var pomodorosAmount = 0

    constructor()

    constructor(pomodoroState: PomodoroState, lastState: PomodoroState, startTime: Long, lastUpdateTime: Long) {
        this.pomodoroState = pomodoroState
        this.lastState = lastState
        this.startTime = startTime
        this.lastUpdateTime = lastUpdateTime
        this.pomodorosAmount = 0
    }

    override fun getState() = this

    @Synchronized override fun loadState(persistence: PomodoroModelPersistence) = XmlSerializerUtil.copyBean(persistence, this)
}
