package pomodoro

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.components.ServiceManager
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
import pomodoro.toolkitwindow.ToolWindows
import pomodoro.widget.PomodoroWidget

class PomodoroComponent : ApplicationComponent {
    private lateinit var timeSource: TimeSource
    private lateinit var userNotifier: UserNotifier
    lateinit var model: PomodoroModel private set

    override fun initComponent() {
        val settings = Settings.instance

        model = PomodoroModel(settings, ServiceManager.getService(PomodoroState::class.java))
        model.onIdeStartup(Time.now())

        val toolWindows = ToolWindows()
        settings.addChangeListener(toolWindows)

        userNotifier = UserNotifier(model)

        val connection = ApplicationManager.getApplication().messageBus.connect()
        connection.subscribe(ProjectManager.TOPIC, object: ProjectManagerListener {
            override fun projectOpened(project: Project) {
                ApplicationManager.getApplication().invokeLater {
                    project.statusBar()?.let {
                        val widget = PomodoroWidget()
                        it.addWidget(widget, "before Position", project)
                        settings.addChangeListener(widget)

                        Disposer.register(project, Disposable { settings.removeChangeListener(widget) })
                    }
                }
            }

            override fun projectClosing(project: Project) {}

            override fun projectClosed(project: Project) {}

            override fun projectClosingBeforeSave(project: Project) {}
        })

        timeSource = TimeSource(listener = { time -> model.onTimer(time) }).start()
    }

    override fun disposeComponent() {
        timeSource.stop()
        userNotifier.dispose()
    }

    override fun getComponentName() = "Pomodoro"


    private class UserNotifier(private val model: PomodoroModel) {
        private val ringSound = RingSound()

        init {
            // See https://github.com/dkandalov/friday-mario/issues/3#issuecomment-160421286 and http://keithp.com/blogs/Java-Sound-on-Linux/
            val clipProperty = System.getProperty("javax.sound.sampled.Clip")
            if (SystemInfo.isLinux && clipProperty != null && clipProperty == "org.classpath.icedtea.pulseaudio.PulseAudioMixerProvider") {
                showPopupNotification(
                        "JDK used by your IDE can lock up or fail to play sounds.<br/>" +
                        "Please see <a href=\"http://keithp.com/blogs/Java-Sound-on-Linux/\">http://keithp.com/blogs/Java-Sound-on-Linux</a> to fix it.")
            }

            model.addListener(this, object : PomodoroModel.Listener {
                override fun onStateChange(state: PomodoroState, wasManuallyStopped: Boolean) {
                    val settings = Settings.instance
                    when (state.mode) {
                        Run -> if (state.lastMode == Break && settings.startNewPomodoroAfterBreak) {
                            ringSound.play(settings.ringVolume)
                            if (settings.isPopupEnabled) showPopupNotification(UIBundle.message("notification.pomodoro_start"))
                        }
                        Stop -> if (state.lastMode == Break && !wasManuallyStopped) {
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
