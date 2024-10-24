package pomodoro.model

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import pomodoro.model.time.Duration
import java.util.*

@State(name = "PomodoroSettings", storages = [Storage("pomodoro.settings.xml")])
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
        var isShowTimeInToolbarWidget: Boolean = true,
        var startNewPomodoroAfterBreak: Boolean = false
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

    override fun getState() = this

    override fun loadState(settings: Settings) {
        XmlSerializerUtil.copyBean(settings, this)
        for (changeListener in changeListeners) {
            changeListener.onChange(this.copy())
        }
    }

    class MinutesConverter : Converter<Duration>() {
        override fun toString(value: Duration) = value.minutes.toString()
        override fun fromString(value: String) = Duration(minutes = value.toInt())
    }

    interface ChangeListener {
        fun onChange(newSettings: Settings)
    }

    companion object {
        val defaultPomodoroDuration = Duration(minutes = 25)
        val defaultBreakDuration = Duration(minutes = 5)
        val defaultLongBreakDuration = Duration(minutes = 20)
        const val defaultLongBreakFrequency = 4
    }
}
