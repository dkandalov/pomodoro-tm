package pomodoro.settings

import com.intellij.openapi.options.ConfigurationException
import pomodoro.model.Settings
import pomodoro.model.time.minutes
import java.awt.Button
import java.awt.FlowLayout
import java.awt.event.ActionListener
import javax.swing.JFrame
import javax.swing.WindowConstants

fun main() {
    val settings = Settings(pomodoroDuration = 123.minutes)
    val presenter = SettingsPresenter(settings)
    val component = presenter.createComponent()
    presenter.reset()

    JFrame().apply {
        contentPane = component

        // add additional buttons for manual testing
        contentPane.layout = FlowLayout()
        contentPane.add(createButton("<Apply>") {
            try {
                println("modified: " + presenter.isModified)
                println(settings)
                presenter.apply()
                println(settings)
            } catch (e: ConfigurationException) {
                e.printStackTrace()
            }
        })
        contentPane.add(createButton("<Reset>") {
            println("modified: " + presenter.isModified)
            println(settings)
            presenter.reset()
            println(settings)
        })

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
