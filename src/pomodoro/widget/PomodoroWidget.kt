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
import pomodoro.model.PomodoroModel
import pomodoro.model.PomodoroState
import pomodoro.model.PomodoroState.Type.*
import pomodoro.model.Settings
import pomodoro.toolkitwindow.PomodoroPresenter
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.time.Instant
import javax.swing.ImageIcon
import javax.swing.JComponent


class PomodoroWidget : CustomStatusBarWidget, StatusBarWidget.Multiframe, Settings.ChangeListener {
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

        model.addUpdateListener(panelWithIcon, object : PomodoroModel.Listener {
            override fun onStateChange(state: PomodoroState, wasManuallyStopped: Boolean) {
                ApplicationManager.getApplication().invokeLater { updateWidgetPanel(model, panelWithIcon, settings.isShowTimeInToolbarWidget) }
            }
        })
        panelWithIcon.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                model.onUserSwitchToNextState(Instant.now())
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
        val pomodorosAmount = model.state.pomodorosAmount
        return UIBundle.message("statuspanel.tooltip", nextAction, pomodorosAmount)
    }

    private fun nextActionName(model: PomodoroModel) = when (model.state.type) {
        STOP -> UIBundle.message("statuspanel.start")
        RUN -> UIBundle.message("statuspanel.stop")
        BREAK -> UIBundle.message("statuspanel.stop_break")
        else -> ""
    }

    private fun updateWidgetPanel(model: PomodoroModel, panelWithIcon: TextPanelWithIcon, showTimeInToolbarWidget: Boolean) {
        if (showTimeInToolbarWidget) {
            panelWithIcon.text = PomodoroPresenter.formatDuration(model.timeLeft)
        } else {
            panelWithIcon.text = ""
        }
        panelWithIcon.setIcon(getIcon(model.state))
        panelWithIcon.repaint()
    }

    private fun getIcon(state: PomodoroState): ImageIcon {
        val underDarcula = UIUtil.isUnderDarcula()
        return when (state.type) {
            STOP -> if (underDarcula) pomodoroStoppedDarculaIcon else pomodoroStoppedIcon
            RUN -> if (underDarcula) pomodoroDarculaIcon else pomodoroIcon
            BREAK -> if (underDarcula) pomodoroBreakDarculaIcon else pomodoroBreakIcon
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
