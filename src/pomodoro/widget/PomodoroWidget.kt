package pomodoro.widget

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.ui.UIUtil
import pomodoro.PomodoroService
import pomodoro.UIBundle
import pomodoro.model.PomodoroModel
import pomodoro.model.PomodoroState
import pomodoro.model.PomodoroState.Mode.*
import pomodoro.model.Settings
import pomodoro.model.time.Duration
import pomodoro.model.time.Time
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.ImageIcon
import javax.swing.JComponent

class PomodoroWidgetFactory : StatusBarWidgetFactory {
    override fun getId() = "Pomodoro"
    override fun getDisplayName() = "Pomodoro"
    override fun isAvailable(project: Project) = true
    override fun createWidget(project: Project) = PomodoroWidget()
    override fun disposeWidget(widget: StatusBarWidget) = widget.dispose()
    override fun canBeEnabledOn(statusBar: StatusBar) = true
}

class PomodoroWidget: CustomStatusBarWidget, StatusBarWidget.Multiframe, Settings.ChangeListener {
    private val panelWithIcon = TextPanelWithIcon()
    private lateinit var statusBar: StatusBar
    private val model = service<PomodoroService>().model

    init {
        val settings = service<Settings>()
        updateWidgetPanel(model, panelWithIcon, settings.isShowTimeInToolbarWidget)

        model.addListener(this, object : PomodoroModel.Listener {
            override fun onStateChange(state: PomodoroState, wasManuallyStopped: Boolean) {
                ApplicationManager.getApplication().invokeLater { updateWidgetPanel(model, panelWithIcon, settings.isShowTimeInToolbarWidget) }
            }
        })
        panelWithIcon.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                model.onUserSwitchToNextState(Time.now())
            }

            override fun mouseEntered(e: MouseEvent?) {
                statusBar.info = UIBundle.message("statuspanel.tooltip", nextActionName(model), model.state.pomodorosAmount)
            }

            override fun mouseExited(e: MouseEvent?) {
                statusBar.info = ""
            }
        })
    }

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun getComponent(): JComponent = panelWithIcon

    override fun copy() = PomodoroWidget()

    private fun nextActionName(model: PomodoroModel) = when (model.state.mode) {
        Run   -> UIBundle.message("statuspanel.stop")
        Break -> UIBundle.message("statuspanel.stop_break")
        Stop  -> UIBundle.message("statuspanel.start")
    }

    private fun updateWidgetPanel(model: PomodoroModel, panelWithIcon: TextPanelWithIcon, showTimeInToolbarWidget: Boolean) {
        panelWithIcon.text = if (showTimeInToolbarWidget) model.timeLeft.formatted() else ""
        panelWithIcon.toolTipText = UIBundle.message("statuspanel.widget_tooltip", nextActionName(model))
        panelWithIcon.icon = when (model.state.mode) {
            Run   -> if (UIUtil.isUnderDarcula()) pomodoroDarculaIcon else pomodoroIcon
            Break -> if (UIUtil.isUnderDarcula()) pomodoroBreakDarculaIcon else pomodoroBreakIcon
            Stop  -> if (UIUtil.isUnderDarcula()) pomodoroStoppedDarculaIcon else pomodoroStoppedIcon
        }
        panelWithIcon.repaint()
    }

    override fun dispose() {
        model.removeListener(this)
    }

    override fun ID() = "Pomodoro"

    override fun onChange(newSettings: Settings) {
        updateWidgetPanel(model, panelWithIcon, newSettings.isShowTimeInToolbarWidget)
    }

    private fun Duration.formatted(): String {
        val formattedMinutes = String.format("%02d", minutes)
        val formattedSeconds = String.format("%02d", (this - Duration(minutes)).seconds)
        return "$formattedMinutes:$formattedSeconds"
    }

    companion object {
        private val pomodoroIcon = loadIcon("/pomodoro.png")
        private val pomodoroStoppedIcon = loadIcon("/pomodoroStopped.png")
        private val pomodoroBreakIcon = loadIcon("/pomodoroBreak.png")
        private val pomodoroDarculaIcon = loadIcon("/pomodoro-inverted.png")
        private val pomodoroStoppedDarculaIcon = loadIcon("/pomodoroStopped-inverted.png")
        private val pomodoroBreakDarculaIcon = loadIcon("/pomodoroBreak-inverted.png")

        private fun loadIcon(filePath: String) = ImageIcon(PomodoroWidget::class.java.getResource(filePath))
    }
}
