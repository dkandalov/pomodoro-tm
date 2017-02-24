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
package pomodoro.toolkitwindow

import pomodoro.UIBundle
import pomodoro.model.PomodoroModel
import pomodoro.model.PomodoroState
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.lang.System.currentTimeMillis
import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.SwingUtilities

class PomodoroPresenter(private val model: PomodoroModel) {
    private val playIcon = ImageIcon(javaClass.getResource("/resources/play-icon.png"))
    private val stopIcon = ImageIcon(javaClass.getResource("/resources/stop-icon.png"))

    private val form = PomodoroForm()
    private var progressBarPrefix = ""

    init {
        form.controlButton.addActionListener {
            model.onUserSwitchToNextState(currentTimeMillis())
            updateUI()
        }
        form.pomodorosLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent?) {
                if (event!!.clickCount >= 2) {
                    model.resetPomodoros()
                    updateUI()
                }
            }
        })
        updateUI()

        model.addUpdateListener(this) { updateUI() }
    }

    private fun updateUI() {
        SwingUtilities.invokeLater {
            when (model.state.type) {
                PomodoroState.Type.RUN -> {
                    form.controlButton.text = UIBundle.message("toolwindow.button_stop")
                    form.controlButton.icon = stopIcon
                    progressBarPrefix = UIBundle.message("toolwindow.prefix_working")
                }
                PomodoroState.Type.STOP -> {
                    form.controlButton.text = UIBundle.message("toolwindow.button_start")
                    form.controlButton.icon = playIcon
                }
                PomodoroState.Type.BREAK -> {
                    form.controlButton.text = UIBundle.message("toolwindow.button_stop")
                    progressBarPrefix = UIBundle.message("toolwindow.button_break")
                }
                else -> throw IllegalStateException()
            }
            form.pomodorosLabel.text = model.state.pomodorosAmount.toString()

            form.progressBar.maximum = model.getProgressMax()
            form.progressBar.value = hack_for_jdk1_6_u06__IDEA_9_0_2__winXP(model.state.progress)

            val timeLeft = model.getProgressMax() - model.state.progress
            form.progressBar.string = progressBarPrefix + " " + formatTime(timeLeft)
        }
    }

    val contentPane: JComponent
        get() = form.rootPanel

    companion object {

        private fun hack_for_jdk1_6_u06__IDEA_9_0_2__winXP(progress: Int): Int {
            // for some reason JProgressBar doesn't display text when progress is too small to be displayed
            return if (progress < 10) 10 else progress
        }

        fun formatTime(timeLeft: Int): String {
            val min = timeLeft / 60
            val sec = timeLeft % 60
            return String.format("%02d", min) + ":" + String.format("%02d", sec)
        }
    }

}
