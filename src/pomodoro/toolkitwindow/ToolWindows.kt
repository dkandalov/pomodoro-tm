package pomodoro.toolkitwindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import pomodoro.PomodoroComponent
import pomodoro.UIBundle
import pomodoro.model.Settings
import javax.swing.ImageIcon

class ToolWindows : Settings.ChangeListener {
    init {
        val connection = ApplicationManager.getApplication().messageBus.connect()
        connection.subscribe(ProjectManager.TOPIC, object: ProjectManagerListener {
            override fun projectOpened(project: Project?) {
                if (project == null) return
                ApplicationManager.getApplication().invokeLater {
                    if (Settings.instance.isShowToolWindow) {
                        registerWindowFor(project)
                    }
                }
            }

            override fun projectClosed(project: Project?) {
                if (project == null) return
                ApplicationManager.getApplication().invokeLater {
                    unregisterWindowFrom(project)
                }
            }
        })
    }

    override fun onChange(newSettings: Settings) {
        for (project in ProjectManager.getInstance().openProjects) {
            if (newSettings.isShowToolWindow) registerWindowFor(project)
            else unregisterWindowFrom(project)
        }
    }

    private fun registerWindowFor(project: Project) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        if (toolWindowManager.getToolWindow(TOOL_WINDOW_ID) != null) {
            return
        }

        val pomodoroComponent = ApplicationManager.getApplication().getComponent(PomodoroComponent::class.java)
        val presenter = ToolwindowPresenter(pomodoroComponent.model)
        Disposer.register(project, presenter)

        val toolWindow: ToolWindow
        toolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.BOTTOM, project, true, true)
        toolWindow.icon = pomodoroIcon
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(presenter.contentPane, UIBundle.message("toolwindow.title"), false)
        toolWindow.contentManager.addContent(content)
    }

    private fun unregisterWindowFrom(project: Project) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        if (toolWindowManager.getToolWindow(TOOL_WINDOW_ID) != null) {
            toolWindowManager.unregisterToolWindow(TOOL_WINDOW_ID)
        }
    }

    companion object {
        const val TOOL_WINDOW_ID = "Pomodoro"

        private val pomodoroIcon = ImageIcon(ToolWindows::class.java.getResource("/resources/pomodoro-icon.png"))
    }
}
