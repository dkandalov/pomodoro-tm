package pomodoro.widget

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.impl.AsyncDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.SPEEDSEARCH
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import pomodoro.PomodoroService
import pomodoro.ResetPomodorosCounter
import pomodoro.StartOrStopPomodoro
import pomodoro.UIBundle
import pomodoro.model.PomodoroModel
import pomodoro.model.PomodoroSnapshot
import pomodoro.model.PomodoroState
import pomodoro.model.PomodoroState.Mode.*
import pomodoro.model.Settings
import pomodoro.model.time.Duration
import pomodoro.model.time.Time
import java.awt.Point
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

class PomodoroWidget : CustomStatusBarWidget, StatusBarWidget.Multiframe, Settings.ChangeListener {
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
            override fun mouseClicked(event: MouseEvent?) {
                if (event == null) return
                if (event.isAltDown) {
                    val popup = JBPopupFactory.getInstance().createActionGroupPopup(
                        null,
                        DefaultActionGroup(listOf(StartOrStopPomodoro(), ResetPomodorosCounter())),
                        MapDataContext(mapOf(PlatformDataKeys.CONTEXT_COMPONENT.name to event.component)),
                        SPEEDSEARCH,
                        true
                    )
                    val dimension = popup.content.preferredSize
                    val point = Point(0, -dimension.height)
                    popup.show(RelativePoint(event.component, point))
                } else if (event.button == MouseEvent.BUTTON1) {
                    model.onUserSwitchToNextState(Time.now())
                }
            }

            override fun mouseEntered(e: MouseEvent?) {
                val (today, week, month) = PomodoroSnapshot.statisticsAt(Time.now(), model.state.history)
                statusBar.info = UIBundle.message("statuspanel.tooltip", nextActionName(model),
                        today.completed, today.failed, week.completed, week.failed, month.completed, month.failed)
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
            Run   -> if (JBColor.isBright()) pomodoroIcon else pomodoroDarculaIcon
            Break -> if (JBColor.isBright()) pomodoroBreakIcon else pomodoroBreakDarculaIcon
            Stop  -> if (JBColor.isBright()) pomodoroStoppedIcon else pomodoroStoppedDarculaIcon
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

    private class MapDataContext(private val map: Map<String, Any?> = HashMap()) : AsyncDataContext {
        override fun getData(dataId: String) = map[dataId]
    }

    companion object {
        private val pomodoroIcon = loadIcon("/pomodoro.png")
        private val pomodoroStoppedIcon = loadIcon("/pomodoroStopped.png")
        private val pomodoroBreakIcon = loadIcon("/pomodoroBreak.png")
        private val pomodoroDarculaIcon = loadIcon("/pomodoro_dark.png")
        private val pomodoroStoppedDarculaIcon = loadIcon("/pomodoroStopped_dark.png")
        private val pomodoroBreakDarculaIcon = loadIcon("/pomodoroBreak_dark.png")

        private fun loadIcon(filePath: String) = ImageIcon(PomodoroWidget::class.java.getResource(filePath))
    }
}
