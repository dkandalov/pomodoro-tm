package pomodoro

import com.intellij.ide.DataManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerAdapter
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import pomodoro.modalwindow.ModalDialog
import pomodoro.model.PomodoroModel
import pomodoro.model.PomodoroState
import pomodoro.model.PomodoroState.Mode.BREAK
import pomodoro.model.PomodoroState.Mode.STOP
import pomodoro.model.Settings
import pomodoro.model.TimeSource
import pomodoro.model.time.Time
import pomodoro.toolkitwindow.PomodoroToolWindows
import pomodoro.widget.PomodoroWidget

class PomodoroComponent : ApplicationComponent {
    private lateinit var timeSource: TimeSource
    lateinit var model: PomodoroModel private set

    override fun initComponent() {
        val settings = settings

        model = PomodoroModel(settings, ServiceManager.getService(PomodoroState::class.java))
        model.onIdeStartup(Time.now())

        val toolWindows = PomodoroToolWindows()
        settings.addChangeListener(toolWindows)

        UserNotifier(settings, model)

        ProjectManager.getInstance().addProjectManagerListener(object : ProjectManagerAdapter() {
            override fun projectOpened(project: Project?) {
                if (project == null) return
                val statusBar = statusBarFor(project)
                val widget = PomodoroWidget()
                statusBar.addWidget(widget, "before Position", project)
                settings.addChangeListener(widget)

                Disposer.register(project, Disposable { settings.removeChangeListener(widget) })
            }
        })

        timeSource = TimeSource(listener = { time -> model.onTimer(time) }).start()
    }

    override fun disposeComponent() {
        timeSource.stop()
    }

    override fun getComponentName(): String {
        return "Pomodoro"
    }


    private class UserNotifier(settings: Settings, model: PomodoroModel) {
        private val ringSound = RingSound()
        private var modalDialog: ModalDialog? = null

        init {
            model.addUpdateListener(this, object : PomodoroModel.Listener {
                override fun onStateChange(state: PomodoroState, wasManuallyStopped: Boolean) {
                    when (state.mode) {
                        STOP -> if (state.lastMode == BREAK && !wasManuallyStopped) {
                            ringSound.play(settings.ringVolume)
                            if (settings.isBlockDuringBreak) unblockIDE()
                        }
                        BREAK -> if (state.lastMode != BREAK) {
                            ringSound.play(settings.ringVolume)
                            if (settings.isPopupEnabled) showPopupNotification()
                            if (settings.isBlockDuringBreak) blockIDE()
                        }
                        else -> {}
                    }
                }
            })
        }

        private fun blockIDE() {
            ApplicationManager.getApplication().invokeLater {
                val dataContext = DataManager.getInstance().getDataContext(IdeFocusManager.getGlobalInstance().focusOwner)
                val project = PlatformDataKeys.PROJECT.getData(dataContext)
                val window = WindowManager.getInstance().getFrame(project)

                modalDialog = ModalDialog(window)
                modalDialog!!.show()
            }
        }

        private fun unblockIDE() {
            if (modalDialog == null) return  // can happen if user turns on this option during break
            modalDialog!!.hide()
        }

        private fun showPopupNotification() {
            ApplicationManager.getApplication().invokeLater(Runnable {
                fun hasPomodoroToolWindow(toolWindowManager: ToolWindowManager): Boolean {
                    return toolWindowManager.toolWindowIds.contains(PomodoroToolWindows.TOOL_WINDOW_ID)
                }

                val dataContext = DataManager.getInstance().dataContextFromFocus.result
                val project = PlatformDataKeys.PROJECT.getData(dataContext) ?: return@Runnable

                val statusMessage = UIBundle.message("notification.text")

                val toolWindowManager = ToolWindowManager.getInstance(project)
                if (hasPomodoroToolWindow(toolWindowManager)) {
                    toolWindowManager.notifyByBalloon(PomodoroToolWindows.TOOL_WINDOW_ID, MessageType.INFO, statusMessage)
                } else {
                    toolWindowManager.notifyByBalloon("Project", MessageType.INFO, statusMessage)
                }
            })
        }
    }

    companion object {
        val settings: Settings
            get() = ServiceManager.getService(Settings::class.java)

        private fun statusBarFor(project: Project): StatusBar {
            return WindowManager.getInstance().getStatusBar(project)
        }
    }
}
