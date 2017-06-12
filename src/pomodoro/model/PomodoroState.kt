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
 * It is not part of [Settings] class because instances of this class cannot be directly changed by user.
 */
@State(name = "PomodoroState", storages = arrayOf(Storage(file = "pomodoro.state.xml")))
data class PomodoroState(
    @Transient var mode: Mode = Stop,
    @OptionTag(nameAttribute = "lastState", converter = ModeConverter::class) var lastMode: Mode = Stop,
    @OptionTag(nameAttribute = "startTime", converter = TimeConverter::class) var startTime: Time = Time.zero,
    @OptionTag(nameAttribute = "lastUpdateTime", converter = TimeConverter::class) var lastUpdateTime: Time = Time.zero,
    @OptionTag(nameAttribute = "pomodorosAmount") var pomodorosAmount: Int = 0,
    @Transient var progress: Duration = Duration.zero,
    var pomodorosTillLongBreak: Int = Settings.defaultLongBreakFrequency
) : PersistentStateComponent<PomodoroState> {

    override fun getState() = this

    override fun loadState(persistence: PomodoroState) = XmlSerializerUtil.copyBean(persistence, this)

    enum class Mode {
        /** Pomodoro in progress. */
        Run,
        /** Pomodoro during break. Can only happen after completing a pomodoro. */
        Break,
        /** Pomodoro timer was not started or was stopped during pomodoro or break. */
        Stop
    }

    private class TimeConverter : Converter<Time>() {
        override fun toString(mode: Time) = mode.epochMilli.toString()
        override fun fromString(value: String) = Time(epochMilli = value.toLong())
    }

    private class ModeConverter : Converter<Mode>() {
        override fun toString(mode: Mode) = mode.name.toUpperCase()
        override fun fromString(value: String) = when (value.toUpperCase()) {
            "RUN" -> Run
            "BREAK" -> Break
            "STOP" -> Stop
            else -> error("Unknown mode: '$value'")
        }
    }
}
