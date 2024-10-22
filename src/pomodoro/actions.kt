package pomodoro

import com.intellij.openapi.actionSystem.ActionUpdateThread.BGT
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import pomodoro.model.PomodoroState.Mode.*
import pomodoro.model.time.Time

class StartOrStopPomodoro : AnAction(), DumbAware {
    override fun actionPerformed(event: AnActionEvent) {
        service<PomodoroService>().model.onUserSwitchToNextState(Time.now())
    }

    override fun update(event: AnActionEvent) {
        event.presentation.text = when (service<PomodoroService>().model.state.mode) {
            Run -> "Stop Pomodoro Timer"
            Break -> "Stop Pomodoro Timer"
            Stop -> "Start Pomodoro Timer"
        }
    }

    override fun getActionUpdateThread() = BGT
}

class ResetPomodorosCounter : AnAction("Reset Pomodoros Counter"), DumbAware {
    override fun actionPerformed(event: AnActionEvent) {
        service<PomodoroService>().model.resetPomodoros()
    }

    override fun getActionUpdateThread() = BGT
}
