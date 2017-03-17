package pomodoro

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import pomodoro.model.time.Time

class PomodoroAction : AnAction(), DumbAware {
    override fun actionPerformed(event: AnActionEvent) {
        val pomodoroComponent = ApplicationManager.getApplication().getComponent(PomodoroComponent::class.java) ?: return
        pomodoroComponent.model.onUserSwitchToNextState(Time.now())
    }
}
