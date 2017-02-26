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
import pomodoro.model.Settings.Companion.defaultPomodoroDuration
import java.time.Duration
import java.util.*

@State(name = "PomodoroSettings", storages = arrayOf(Storage(id = "other", file = "\$APP_CONFIG$/pomodoro.settings.xml")))
data class Settings(
        @OptionTag(nameAttribute = "pomodoroLengthInMinutes", converter = MinutesConverter::class)
        var pomodoroDuration: Duration = defaultPomodoroDuration,
        @OptionTag(nameAttribute = "breakLengthInMinutes", converter = MinutesConverter::class)
        var breakDuration: Duration = defaultBreakDuration,
        @OptionTag(converter = MinutesConverter::class)
        var longBreakDuration: Duration = defaultLongBreakDuration,
        var longBreakFrequency: Int = defaultLongBreakFrequency,
        var ringVolume: Int = 1,
        var isPopupEnabled: Boolean = true,
        var isBlockDuringBreak: Boolean = false,
        var isShowToolWindow: Boolean = false,
        var isShowTimeInToolbarWidget: Boolean = true
) : PersistentStateComponent<Settings> {
    /**
     * If IntelliJ shuts down during pomodoro and then restarts, pomodoro can be continued.
     * This property determines how much time can pass before we consider pomodoro to be expired.
     * @return timeout in milliseconds
     */
    val timeoutToContinuePomodoro = defaultBreakDuration
    private val changeListeners = ArrayList<ChangeListener>()

    fun addChangeListener(changeListener: ChangeListener) {
        changeListeners.add(changeListener)
    }

    fun removeChangeListener(changeListener: ChangeListener) {
        changeListeners.remove(changeListener)
    }

    override fun getState(): Settings? {
        return this
    }

    override fun loadState(settings: Settings) {
        XmlSerializerUtil.copyBean(settings, this)
        for (changeListener in changeListeners) {
            changeListener.onChange(this)
        }
    }

    class MinutesConverter : Converter<Duration>() {
        override fun toString(t: Duration) = t.toMinutes().toString()
        override fun fromString(value: String) = Duration.ofMinutes(value.toLong())!!
    }

    interface ChangeListener {
        fun onChange(settings: Settings)
    }

    companion object {
        val defaultPomodoroDuration = Duration.ofMinutes(25)!!
        val defaultBreakDuration = Duration.ofMinutes(5)!!
        val defaultLongBreakDuration = Duration.ofMinutes(20)!!
        const val defaultLongBreakFrequency = 4
    }
}
