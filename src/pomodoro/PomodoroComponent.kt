package pomodoro

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.WindowManager
import pomodoro.model.PomodoroModel
import pomodoro.model.PomodoroState
import pomodoro.model.PomodoroState.Mode.*
import pomodoro.model.Settings
import pomodoro.model.TimeSource
import pomodoro.model.time.Time
import pomodoro.widget.PomodoroWidget

@Service
class PomodoroComponent : Disposable {
    private val timeSource: TimeSource
    private val userNotifier: UserNotifier
    val model: PomodoroModel

    init {
        val settings = service<Settings>()

        model = PomodoroModel(settings, service())
        model.onIdeStartup(Time.now())

        userNotifier = UserNotifier(model)

        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(ProjectManager.TOPIC, object : ProjectManagerListener {
                override fun projectOpened(project: Project) {
                    ApplicationManager.getApplication().invokeLater {
                        project.statusBar()?.let {
                            val widget = PomodoroWidget()
                            it.addWidget(widget, "before Position", project)
                            settings.addChangeListener(widget)

                            Disposer.register(project) { settings.removeChangeListener(widget) }
                        }
                    }
                }
            })

        timeSource = TimeSource(listener = { time -> model.onTimer(time) }).start()
    }

    override fun dispose() {
        timeSource.stop()
        userNotifier.dispose()
    }

    private class UserNotifier(private val model: PomodoroModel) {
        private val ringSound = RingSound()

        init {
            // See https://github.com/dkandalov/friday-mario/issues/3#issuecomment-160421286 and http://keithp.com/blogs/Java-Sound-on-Linux/
            val clipProperty = System.getProperty("javax.sound.sampled.Clip")
            if (SystemInfo.isLinux && clipProperty != null && clipProperty == "org.classpath.icedtea.pulseaudio.PulseAudioMixerProvider") {
                showPopupNotification(
                    "JDK used by your IDE can lock up or fail to play sounds.<br/>" +
                        "Please see <a href=\"https://keithp.com/blogs/Java-Sound-on-Linux\">https://keithp.com/blogs/Java-Sound-on-Linux</a> to fix it."
                )
            }

            model.addListener(this, object : PomodoroModel.Listener {
                override fun onStateChange(state: PomodoroState, wasManuallyStopped: Boolean) {
                    val settings = service<Settings>()
                    when (state.mode) {
                        Run   -> if (state.lastMode == Break && settings.startNewPomodoroAfterBreak) {
                            ringSound.play(settings.ringVolume)
                            if (settings.isPopupEnabled) showPopupNotification(UIBundle.message("notification.pomodoro_start"))
                        }
                        Stop  -> if (state.lastMode == Break && !wasManuallyStopped) {
                            ringSound.play(settings.ringVolume)
                        }
                        Break -> if (state.lastMode != Break) {
                            ringSound.play(settings.ringVolume)
                            if (settings.isPopupEnabled) showPopupNotification(UIBundle.message("notification.text"))
                        }
                    }
                }
            })
        }

        private fun showPopupNotification(message: String) {
            ApplicationManager.getApplication().invokeLater {
                val groupDisplayId = "Pomodoro Notifications"
                val title = ""
                val notification = Notification(groupDisplayId, title, message, NotificationType.INFORMATION)
                ApplicationManager.getApplication().messageBus.syncPublisher(Notifications.TOPIC).notify(notification)
            }
        }

        fun dispose() {
            model.removeListener(this)
        }
    }

    private fun Project.statusBar(): StatusBar? = WindowManager.getInstance().getStatusBar(this)
}
