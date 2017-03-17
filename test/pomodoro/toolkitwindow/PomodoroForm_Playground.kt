package pomodoro.toolkitwindow

import pomodoro.model.PomodoroModel
import pomodoro.model.PomodoroState
import pomodoro.model.Settings
import pomodoro.model.TimeSource
import pomodoro.model.time.Duration
import javax.swing.JFrame
import javax.swing.WindowConstants

fun main(args: Array<String>) {
    val config = Settings(pomodoroDuration = Duration(1), breakDuration = Duration(1))
    val model = PomodoroModel(config, PomodoroState())
    val presenter = PomodoroPresenter(model)
    TimeSource({ model.onTimer(it) }).start()

    JFrame().apply {
        contentPane = presenter.contentPane
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        pack()
        isVisible = true
    }
}
