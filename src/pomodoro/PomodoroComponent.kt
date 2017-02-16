/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import pomodoro.model.ControlThread
import pomodoro.model.PomodoroModel
import pomodoro.model.PomodoroModelPersistence
import pomodoro.model.Settings
import pomodoro.toolkitwindow.PomodoroToolWindows
import pomodoro.widget.PomodoroWidget
import javax.swing.SwingUtilities

class PomodoroComponent : ApplicationComponent {
    private lateinit var controlThread: ControlThread
    lateinit var model: PomodoroModel private set

    override fun initComponent() {
        val settings = settings

        model = PomodoroModel(settings, ServiceManager.getService(PomodoroModelPersistence::class.java))

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

        controlThread = ControlThread(model)
        controlThread.start()
    }

    override fun disposeComponent() {
        controlThread.shouldStop()
    }

    override fun getComponentName(): String {
        return "Pomodoro"
    }


    private class UserNotifier(settings: Settings, model: PomodoroModel) {
        private val ringSound = RingSound()
        private var modalDialog: ModalDialog? = null

        init {
            model.addUpdateListener(this) {
                when (model.state) {
                    PomodoroModel.PomodoroState.STOP -> if (model.lastState == PomodoroModel.PomodoroState.BREAK && !model.wasManuallyStopped()) {
                        ringSound.play(settings.ringVolume)
                        if (settings.isBlockDuringBreak) unblockIntelliJ()
                    }
                    PomodoroModel.PomodoroState.BREAK -> if (model.lastState != PomodoroModel.PomodoroState.BREAK) {
                        ringSound.play(settings.ringVolume)
                        if (settings.isPopupEnabled) showPopupNotification()
                        if (settings.isBlockDuringBreak) blockIntelliJ()
                    }
                }
            }
        }

        private fun blockIntelliJ() {
            ApplicationManager.getApplication().invokeLater {
                val dataContext = DataManager.getInstance().getDataContext(IdeFocusManager.getGlobalInstance().focusOwner)
                val project = PlatformDataKeys.PROJECT.getData(dataContext)
                val window = WindowManager.getInstance().getFrame(project)

                modalDialog = ModalDialog(window)
                modalDialog!!.show()
            }
        }

        private fun unblockIntelliJ() {
            if (modalDialog == null) return  // can happen if user turns on this option during break
            modalDialog!!.hide()
        }

        private fun showPopupNotification() {
            SwingUtilities.invokeLater(object : Runnable {
                override fun run() {
                    val dataContext = DataManager.getInstance().dataContextFromFocus.result
                    val project = PlatformDataKeys.PROJECT.getData(dataContext) ?: return

                    val statusMessage = UIBundle.message("notification.text")

                    val toolWindowManager = ToolWindowManager.getInstance(project)
                    if (hasPomodoroToolWindow(toolWindowManager)) {
                        toolWindowManager.notifyByBalloon(PomodoroToolWindows.TOOL_WINDOW_ID, MessageType.INFO, statusMessage)
                    } else {
                        toolWindowManager.notifyByBalloon("Project", MessageType.INFO, statusMessage)
                    }
                }

                private fun hasPomodoroToolWindow(toolWindowManager: ToolWindowManager): Boolean {
                    return toolWindowManager.toolWindowIds.contains(PomodoroToolWindows.TOOL_WINDOW_ID)
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
