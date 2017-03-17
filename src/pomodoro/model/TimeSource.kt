package pomodoro.model

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import pomodoro.model.time.Time

/**
 * (Note that [com.intellij.openapi.actionSystem.ActionManager.addTimerListener] won't work as a timer callback)
 */
class TimeSource(private val listener: (Time) -> Unit) {
    @Volatile private var shouldStop = false

    fun start(): TimeSource {
        val application = ApplicationManager.getApplication()
        application.executeOnPooledThread {
            while (!shouldStop) {
                application.invokeLater(
                        { listener(Time.now()) },
                        ModalityState.any() // Use "any" so that timer is updated even while modal dialog like IDE Settings is open.
                )
                Thread.sleep(500)
            }
        }
        return this
    }

    fun stop() {
        shouldStop = true
    }
}
