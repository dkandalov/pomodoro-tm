package pomodoro.toolkitwindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import pomodoro.PomodoroComponent
import pomodoro.UIBundle
import pomodoro.model.Settings
import javax.swing.ImageIcon

class PomodoroToolWindows : Settings.ChangeListener {
    init {
        ProjectManager.getInstance().addProjectManagerListener(object : ProjectManagerListener {
            override fun projectOpened(project: Project?) {
                if (project == null) return
                if (PomodoroComponent.settings.isShowToolWindow) {
                    registerWindowFor(project)
                }
            }

            override fun projectClosed(project: Project?) {
                if (project == null) return
                unregisterWindowFrom(project)
            }

            override fun canCloseProject(project: Project?) = true

            override fun projectClosing(project: Project?) {}
        })
    }

    override fun onChange(settings: Settings) {
        for (project in ProjectManager.getInstance().openProjects) {
            if (settings.isShowToolWindow) registerWindowFor(project)
            else unregisterWindowFrom(project)
        }
    }

    private fun registerWindowFor(project: Project) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        if (toolWindowManager.getToolWindow(TOOL_WINDOW_ID) != null) {
            return
        }

        val pomodoroComponent = ApplicationManager.getApplication().getComponent(PomodoroComponent::class.java)
        val presenter = PomodoroPresenter(pomodoroComponent.model)

        val myToolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.BOTTOM)
        myToolWindow.icon = pomodoroIcon
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(presenter.contentPane, UIBundle.message("toolwindow.title"), false)
        myToolWindow.contentManager.addContent(content)
    }

    private fun unregisterWindowFrom(project: Project) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        if (toolWindowManager.getToolWindow(TOOL_WINDOW_ID) != null) {
            toolWindowManager.unregisterToolWindow(TOOL_WINDOW_ID)
        }
    }

    companion object {
        const val TOOL_WINDOW_ID = "Pomodoro"

        private val pomodoroIcon = ImageIcon(PomodoroToolWindows::class.java.getResource("/resources/pomodoro-icon.png"))
    }
}
