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
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Transient
import pomodoro.model.PomodoroState.Type
import pomodoro.model.PomodoroState.Type.BREAK
import pomodoro.model.PomodoroState.Type.RUN
import pomodoro.model.time.Duration
import pomodoro.model.time.Time

/**
 * Class for persisting pomodoro state.
 * It was not implemented as part of [Settings] class because instances of this class cannot be directly changed by user.
 */
@State(name = "PomodoroState", storages = arrayOf(Storage(id = "other", file = "\$APP_CONFIG$/pomodoro.settings.xml")))
data class PomodoroState(
        @Transient var type: Type = Type.STOP,
        @OptionTag(nameAttribute = "lastState", converter = EnumConverter::class) var lastState: Type = Type.STOP,
        @OptionTag(nameAttribute = "startTime", converter = TimeConverter::class) var startTime: Time = Time.ZERO,
        @OptionTag(nameAttribute = "lastUpdateTime", converter = TimeConverter::class) var lastUpdateTime: Time = Time.ZERO,
        @OptionTag(nameAttribute = "pomodorosAmount") var pomodorosAmount: Int = 0,
        @Transient var progress: Duration = Duration.ZERO
) : PersistentStateComponent<PomodoroState> {

    override fun getState() = this

    override fun loadState(persistence: PomodoroState) = XmlSerializerUtil.copyBean(persistence, this)

    enum class Type {
        /** Pomodoro timer was not started or was stopped during pomodoro or break. */
        STOP,
        /** Pomodoro in progress. */
        RUN,
        /** Pomodoro during break. Can only happen after completing a pomodoro. */
        BREAK
    }

    private class TimeConverter : Converter<Time>() {
        override fun toString(t: Time) = t.epochMilli.toString()
        override fun fromString(value: String) = Time(epochMilli = value.toLong())
    }

    private class EnumConverter : Converter<Type>() {
        override fun toString(t: Type) = t.name
        override fun fromString(value: String): Type? = when (value) {
            "STOP" -> Type.STOP
            "RUN" -> RUN
            "BREAK" -> BREAK
            else -> Type.valueOf(value)
        }
    }
}
