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
data class Settings(
        var pomodoroLengthInMinutes: Int = defaultPomodoroLength,
        var breakLengthInMinutes: Int = defaultBreakLength,
        var longBreakLengthInMinutes: Int = defaultLongBreakLength,
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
    val timeoutToContinuePomodoro = MILLISECONDS.convert(defaultBreakLength.toLong(), MINUTES)
    private val changeListeners = ArrayList<ChangeListener>()


    val pomodoroLengthInMillis: Long
        get() = MINUTES.toMillis(pomodoroLengthInMinutes.toLong())

    val breakLengthInMillis: Long
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

    companion object {
        const val defaultPomodoroLength = 25
        const val defaultBreakLength = 5
        const val defaultLongBreakLength = 20
        const val defaultLongBreakFrequency = 4
    }
}
