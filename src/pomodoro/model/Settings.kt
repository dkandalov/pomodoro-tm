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
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES

@State(name = "PomodoroSettings", storages = arrayOf(Storage(id = "other", file = "\$APP_CONFIG$/pomodoro.settings.xml")))
open class Settings : PersistentStateComponent<Settings> {

    var pomodoroLengthInMinutes = DEFAULT_POMODORO_LENGTH
    var breakLengthInMinutes = DEFAULT_BREAK_LENGTH
    var longBreakLengthInMinutes = DEFAULT_LONG_BREAK_LENGTH
    var longBreakFrequency = DEFAULT_LONG_BREAK_FREQUENCY
    var ringVolume = 1
    var isPopupEnabled = true
    var isBlockDuringBreak = false
    var isShowToolWindow = false
    var isShowTimeInToolbarWidget = true

    /**
     * If IntelliJ shuts down during pomodoro and then restarts, pomodoro can be continued.
     * This property determines how much time can pass before we consider pomodoro to be expired.
     * @return timeout in milliseconds
     */
    val timeoutToContinuePomodoro = MILLISECONDS.convert(DEFAULT_BREAK_LENGTH.toLong(), MINUTES)
    private val changeListeners = ArrayList<ChangeListener>()


    open val pomodoroLengthInMillis: Long
        get() = MINUTES.toMillis(pomodoroLengthInMinutes.toLong())

    open val breakLengthInMillis: Long
        get() = MINUTES.toMillis(breakLengthInMinutes.toLong())

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

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val settings = o as Settings?

        if (isBlockDuringBreak != settings!!.isBlockDuringBreak) return false
        if (breakLengthInMinutes != settings.breakLengthInMinutes) return false
        if (longBreakLengthInMinutes != settings.longBreakLengthInMinutes) return false
        if (longBreakFrequency != settings.longBreakFrequency) return false
        if (pomodoroLengthInMinutes != settings.pomodoroLengthInMinutes) return false
        if (isPopupEnabled != settings.isPopupEnabled) return false
        if (ringVolume != settings.ringVolume) return false
        if (isShowToolWindow != settings.isShowToolWindow) return false
        if (isShowTimeInToolbarWidget != settings.isShowTimeInToolbarWidget) return false
        if (timeoutToContinuePomodoro != settings.timeoutToContinuePomodoro) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pomodoroLengthInMinutes
        result = 31 * result + breakLengthInMinutes
        result = 31 * result + longBreakLengthInMinutes
        result = 31 * result + longBreakFrequency
        result = 31 * result + ringVolume
        result = 31 * result + if (isPopupEnabled) 1 else 0
        result = 31 * result + if (isBlockDuringBreak) 1 else 0
        result = 31 * result + if (isShowToolWindow) 1 else 0
        result = 31 * result + if (isShowTimeInToolbarWidget) 1 else 0
        result = 31 * result + (timeoutToContinuePomodoro xor timeoutToContinuePomodoro.ushr(32)).toInt()
        return result
    }

    override fun toString(): String {
        return "Settings{" +
                "pomodoroLength=" + pomodoroLengthInMinutes +
                ", breakLength=" + breakLengthInMinutes +
                ", longBreakLength=" + longBreakLengthInMinutes +
                ", longBreakFrequency=" + longBreakFrequency +
                ", ringVolume=" + ringVolume +
                ", popupEnabled=" + isPopupEnabled +
                ", blockDuringBreak=" + isBlockDuringBreak +
                ", showToolWindow=" + isShowToolWindow +
                ", showTimeInToolbarWidget=" + isShowTimeInToolbarWidget +
                ", timeoutToContinuePomodoro=" + timeoutToContinuePomodoro +
                '}'
    }

    companion object {
        val DEFAULT_POMODORO_LENGTH = 25
        val DEFAULT_BREAK_LENGTH = 5
        val DEFAULT_LONG_BREAK_LENGTH = 20
        val DEFAULT_LONG_BREAK_FREQUENCY = 4
    }
}
