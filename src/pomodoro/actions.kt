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
            PomodoroState.Mode.Run   -> "Stop Pomodoro Timer"
            PomodoroState.Mode.Break -> "Stop Pomodoro Timer"
            PomodoroState.Mode.Stop  -> "Start Pomodoro Timer"
        }
    }
}

class ResetPomodorosCounter : AnAction("Reset Pomodoros Counter"), DumbAware {
    override fun actionPerformed(event: AnActionEvent) {
        service<PomodoroService>().model.resetPomodoros()
    }
}
