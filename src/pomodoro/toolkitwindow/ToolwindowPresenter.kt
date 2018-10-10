package pomodoro.toolkitwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import pomodoro.UIBundle
import pomodoro.model.PomodoroModel
import pomodoro.model.PomodoroState
import pomodoro.model.PomodoroState.Mode.*
import pomodoro.model.time.Duration
import pomodoro.model.time.Time
import pomodoro.widget.PomodoroWidget
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.ImageIcon
import javax.swing.JComponent

class ToolwindowPresenter(private val model: PomodoroModel): Disposable {
    private val form = PomodoroForm()
    private var progressBarPrefix = ""

    val contentPane: JComponent
        get() = form.rootPanel

    init {
        form.controlButton.addActionListener {
            model.onUserSwitchToNextState(Time.now())
            updateUI(model.state)
        }
        form.pomodorosLabel.addMouseListener(object: MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                if (event.clickCount >= 2) {
                    model.resetPomodoros()
                    updateUI(model.state)
                }
            }
        })
        updateUI(model.state)

        model.addListener(this, object: PomodoroModel.Listener {
            override fun onStateChange(state: PomodoroState, wasManuallyStopped: Boolean) {
                updateUI(state)
            }
        })
    }

    private fun updateUI(state: PomodoroState) {
        ApplicationManager.getApplication().invokeLater {
            when (state.mode) {
                Run   -> {
                    form.controlButton.text = UIBundle.message("toolwindow.button_stop")
                    form.controlButton.icon = stopIcon
                    progressBarPrefix = UIBundle.message("toolwindow.prefix_working")
                }
                Break -> {
                    form.controlButton.text = UIBundle.message("toolwindow.button_stop")
                    progressBarPrefix = UIBundle.message("toolwindow.button_break")
                }
                Stop  -> {
                    form.controlButton.text = UIBundle.message("toolwindow.button_start")
                    form.controlButton.icon = playIcon
                }
            }
            form.pomodorosLabel.text = state.pomodorosAmount.toString()

            form.progressBar.maximum = model.progressMax.toProgress()
            form.progressBar.value = state.progress.toProgress()

            form.progressBar.string = progressBarPrefix + " " + formatDuration(model.timeLeft)
        }
    }

    override fun dispose() {
        model.removeListener(this)
    }

    companion object {
        private val playIcon = loadIcon("/play-icon.png")
        private val stopIcon = loadIcon("/stop-icon.png")

        fun formatDuration(timeLeft: Duration): String {
            val minutes = timeLeft.minutes
            val seconds = (timeLeft - Duration(timeLeft.minutes)).seconds
            return String.format("%02d", minutes) + ":" + String.format("%02d", seconds)
        }

        private fun Duration.toProgress(): Int = (millis / 1000).toInt()

        private fun loadIcon(filePath: String) = ImageIcon(PomodoroWidget::class.java.getResource(filePath))
    }
}
