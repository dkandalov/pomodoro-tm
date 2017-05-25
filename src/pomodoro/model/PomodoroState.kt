package pomodoro.model

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Transient
import pomodoro.model.PomodoroState.Mode.*
import pomodoro.model.time.Duration
import pomodoro.model.time.Time

/**
 * Class for persisting pomodoro state.
 * It was not implemented as part of [Settings] class because instances of this class cannot be directly changed by user.
 */
@State(name = "PomodoroState", storages = arrayOf(Storage(id = "other", file = "\$APP_CONFIG$/pomodoro.state.xml")))
data class PomodoroState(
    @Transient var mode: Mode = STOP,
    @OptionTag(nameAttribute = "lastState", converter = ModeConverter::class) var lastMode: Mode = STOP,
    @OptionTag(nameAttribute = "startTime", converter = TimeConverter::class) var startTime: Time = Time.ZERO,
    @OptionTag(nameAttribute = "lastUpdateTime", converter = TimeConverter::class) var lastUpdateTime: Time = Time.ZERO,
    @OptionTag(nameAttribute = "pomodorosAmount") var pomodorosAmount: Int = 0,
    @Transient var progress: Duration = Duration.ZERO,
    var pomodorosTillLongBreak: Int = Settings.defaultLongBreakFrequency
) : PersistentStateComponent<PomodoroState> {

    override fun getState() = this

    override fun loadState(persistence: PomodoroState) = XmlSerializerUtil.copyBean(persistence, this)

    enum class Mode {
        /** Pomodoro in progress. */
        RUN,
        /** Pomodoro during break. Can only happen after completing a pomodoro. */
        BREAK,
        /** Pomodoro timer was not started or was stopped during pomodoro or break. */
        STOP
    }

    private class TimeConverter : Converter<Time>() {
        override fun toString(t: Time) = t.epochMilli.toString()
        override fun fromString(value: String) = Time(epochMilli = value.toLong())
    }

    private class ModeConverter : Converter<Mode>() {
        override fun toString(t: Mode) = t.name
        override fun fromString(value: String): Mode? = when (value) {
            "RUN" -> RUN
            "BREAK" -> BREAK
            "STOP" -> STOP
            else -> Mode.valueOf(value)
        }
    }
}
