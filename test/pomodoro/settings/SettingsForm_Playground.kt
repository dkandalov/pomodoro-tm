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
package pomodoro.settings

import com.intellij.openapi.options.ConfigurationException
import pomodoro.model.Settings
import pomodoro.model.time.minutes
import java.awt.Button
import java.awt.FlowLayout
import java.awt.event.ActionListener
import javax.swing.JFrame
import javax.swing.WindowConstants

fun main(args: Array<String>) {
    val settings = Settings(pomodoroDuration = 123.minutes)
    val presenter = SettingsPresenter(settings)
    val component = presenter.createComponent()
    presenter.reset()

    JFrame().apply {
        contentPane = component

        // add additional buttons for manual testing
        contentPane.layout = FlowLayout()
        contentPane.add(createButton("<Apply>", ActionListener {
            try {
                println("modified: " + presenter.isModified)
                println(settings)
                presenter.apply()
                println(settings)
            } catch (e: ConfigurationException) {
                e.printStackTrace()
            }
        }))
        contentPane.add(createButton("<Reset>", ActionListener {
            println("modified: " + presenter.isModified)
            println(settings)
            presenter.reset()
            println(settings)
        }))

        title = presenter.displayName
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        pack()
        isVisible = true
    }
}

private fun createButton(label: String, actionListener: ActionListener): Button {
    return Button(label).apply {
        addActionListener(actionListener)
    }
}
