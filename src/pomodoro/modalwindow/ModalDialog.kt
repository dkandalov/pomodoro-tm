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
package pomodoro.modalwindow

import com.intellij.util.ui.UIUtil
import java.awt.Dialog
import java.awt.Frame
import java.awt.Window
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ESCAPE
import javax.swing.JDialog
import javax.swing.SwingUtilities

class ModalDialog(window: Window) {
    private val jDialog: JDialog

    init {
        val model = FormModel()

        if (window is Dialog) {
            jDialog = JDialog(window)
        } else {
            jDialog = JDialog(window as Frame)
        }

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
        SwingUtilities.invokeLater { jDialog.isVisible = true }
    }

    fun hide() {
        SwingUtilities.invokeLater { jDialog.dispose() }
    }
}
