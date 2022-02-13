package pomodoro

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import pomodoro.model.time.Time

class PomodoroAction : AnAction(), DumbAware {
    override fun actionPerformed(event: AnActionEvent) {
        service<PomodoroService>().model.onUserSwitchToNextState(Time.now())
    }
}
