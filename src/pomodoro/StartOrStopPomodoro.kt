package pomodoro

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import pomodoro.model.PomodoroState
import pomodoro.model.time.Time

class StartOrStopPomodoro : AnAction(), DumbAware {
    override fun actionPerformed(event: AnActionEvent) {
        service<PomodoroService>().model.onUserSwitchToNextState(Time.now())
    }

    override fun update(event: AnActionEvent) {
        val mode = service<PomodoroService>().model.state.mode
        event.presentation.text = when (mode) {
            PomodoroState.Mode.Run   -> "Stop Pomodoro"
            PomodoroState.Mode.Break -> "Stop Pomodoro"
            PomodoroState.Mode.Stop  -> "Start Pomodoro"
        }
    }
}

class ResetPomodorosCounter : AnAction("Reset Pomodoros Counter"), DumbAware {
    override fun actionPerformed(event: AnActionEvent) {
        service<PomodoroService>().model.resetPomodoros()
    }
}
