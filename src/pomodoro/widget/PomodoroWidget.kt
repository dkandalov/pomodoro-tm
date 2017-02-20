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
package pomodoro.widget

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.util.ui.UIUtil
import pomodoro.PomodoroComponent
import pomodoro.UIBundle
import pomodoro.model.ChangeListener
import pomodoro.model.PomodoroModel
import pomodoro.model.Settings
import pomodoro.toolkitwindow.PomodoroPresenter

import javax.swing.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.lang.System.currentTimeMillis


class PomodoroWidget : CustomStatusBarWidget, StatusBarWidget.Multiframe, ChangeListener {
    private val pomodoroIcon = ImageIcon(javaClass.getResource("/resources/pomodoro.png"))
    private val pomodoroStoppedIcon = ImageIcon(javaClass.getResource("/resources/pomodoroStopped.png"))
    private val pomodoroBreakIcon = ImageIcon(javaClass.getResource("/resources/pomodoroBreak.png"))
    private val pomodoroDarculaIcon = ImageIcon(javaClass.getResource("/resources/pomodoro-inverted.png"))
    private val pomodoroStoppedDarculaIcon = ImageIcon(javaClass.getResource("/resources/pomodoroStopped-inverted.png"))
    private val pomodoroBreakDarculaIcon = ImageIcon(javaClass.getResource("/resources/pomodoroBreak-inverted.png"))
    private val panelWithIcon = TextPanelWithIcon()
    private lateinit var statusBar: StatusBar
    private val model: PomodoroModel

    init {
        val settings = PomodoroComponent.settings

        val pomodoroComponent = ApplicationManager.getApplication().getComponent(PomodoroComponent::class.java)!!
        model = pomodoroComponent.model
        updateWidgetPanel(model, panelWithIcon, settings.isShowTimeInToolbarWidget)

        model.addUpdateListener(panelWithIcon) { SwingUtilities.invokeLater { updateWidgetPanel(model, panelWithIcon, settings.isShowTimeInToolbarWidget) } }
        panelWithIcon.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                model.onUserSwitchToNextState(currentTimeMillis())
            }

            override fun mouseEntered(e: MouseEvent?) {
                val tooltipText = tooltipText(model)
                statusBar.info = tooltipText
            }

            override fun mouseExited(e: MouseEvent?) {
                statusBar.info = ""
            }
        })
    }

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun getComponent(): JComponent {
        return panelWithIcon
    }

    override fun copy(): StatusBarWidget {
        return PomodoroWidget()
    }

    private fun tooltipText(model: PomodoroModel): String {
        val nextAction = nextActionName(model)
        val pomodorosAmount = model.pomodoros
        return UIBundle.message("statuspanel.tooltip", nextAction, pomodorosAmount)
    }

    private fun nextActionName(model: PomodoroModel): String {
        when (model.state) {
            PomodoroModel.PomodoroState.STOP -> return UIBundle.message("statuspanel.start")
            PomodoroModel.PomodoroState.RUN -> return UIBundle.message("statuspanel.stop")
            PomodoroModel.PomodoroState.BREAK -> return UIBundle.message("statuspanel.stop_break")
            else -> return ""
        }
    }

    private fun updateWidgetPanel(model: PomodoroModel, panelWithIcon: TextPanelWithIcon, showTimeInToolbarWidget: Boolean) {
        if (showTimeInToolbarWidget) {
            val timeLeft = model.getProgressMax() - model.progress
            panelWithIcon.text = PomodoroPresenter.formatTime(timeLeft)
        } else {
            panelWithIcon.text = ""
        }
        panelWithIcon.setIcon(getIcon(model))
        panelWithIcon.repaint()
    }

    private fun getIcon(model: PomodoroModel): ImageIcon {
        val underDarcula = UIUtil.isUnderDarcula()
        when (model.state) {
            PomodoroModel.PomodoroState.STOP -> return if (underDarcula) pomodoroStoppedDarculaIcon else pomodoroStoppedIcon
            PomodoroModel.PomodoroState.RUN -> return if (underDarcula) pomodoroDarculaIcon else pomodoroIcon
            PomodoroModel.PomodoroState.BREAK -> return if (underDarcula) pomodoroBreakDarculaIcon else pomodoroBreakIcon
            else -> throw IllegalStateException()
        }
    }

    override fun getPresentation(type: StatusBarWidget.PlatformType): StatusBarWidget.WidgetPresentation? {
        return null
    }

    override fun dispose() {}

    override fun ID() = "Pomodoro"

    override fun onChange(settings: Settings) {
        updateWidgetPanel(model, panelWithIcon, settings.isShowTimeInToolbarWidget)
    }
}
