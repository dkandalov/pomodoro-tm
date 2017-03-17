package pomodoro.modalwindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.ui.UIUtil
import java.awt.Dialog
import java.awt.Frame
import java.awt.Window
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ESCAPE
import javax.swing.JDialog

class ModalDialog(window: Window) {
    private val jDialog: JDialog

    init {
        val model = FormModel()

        jDialog = JDialog(window as? Dialog ?: window as Frame)

        val keyAdapter = object : KeyAdapter() {
            override fun keyPressed(keyEvent: KeyEvent?) {
                if (keyEvent!!.keyCode == VK_ESCAPE && model.intellijIsAllowedToBeUnlocked()) {
                    jDialog.dispose()
                }
            }
        }

        UIUtil.suppressFocusStealing(window)

        val form = ModalForm(model)
        jDialog.isModal = true
        jDialog.isUndecorated = true
        jDialog.contentPane = form.rootPanel
        jDialog.addKeyListener(keyAdapter)
        jDialog.pack()
        jDialog.setLocationRelativeTo(window)
    }

    fun show() {
        ApplicationManager.getApplication().invokeLater { jDialog.isVisible = true }
    }

    fun hide() {
        ApplicationManager.getApplication().invokeLater { jDialog.dispose() }
    }
}
