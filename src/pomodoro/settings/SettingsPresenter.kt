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
import com.intellij.openapi.options.SearchableConfigurable
import org.jetbrains.annotations.Nls
import pomodoro.PomodoroComponent
import pomodoro.RingSound
import pomodoro.UIBundle
import pomodoro.model.Settings
import pomodoro.model.time.minutes
import java.awt.event.ActionListener
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.event.ChangeListener

class SettingsPresenter constructor(private val settings: Settings = PomodoroComponent.settings) : SearchableConfigurable {
    private var settingsForm: SettingsForm? = null
    private lateinit var uiModel: Settings
    private var updatingUI: Boolean = false
    private val ringSound = RingSound()
    private var lastUIRingVolume = -1

    override fun createComponent(): JComponent? {
        settingsForm = SettingsForm()
        uiModel = Settings()

        setupUIBindings()

        return settingsForm!!.rootPanel
    }

    private fun setupUIBindings() {
        lastUIRingVolume = uiModel.ringVolume
        val actionListener = ActionListener {
            updateUIModel()
            updateUI()
        }
        val changeListener = ChangeListener { actionListener.actionPerformed(null) }
        settingsForm!!.apply {
            pomodoroLengthComboBox.addActionListener(actionListener)
            breakLengthComboBox.addActionListener(actionListener)
            longBreakLengthComboBox.addActionListener(actionListener)
            longBreakFrequencyComboBox.addActionListener(actionListener)
            popupCheckBox.addChangeListener(changeListener)
            blockDuringBreak.addChangeListener(changeListener)
            ringVolumeSlider.addChangeListener(changeListener)
            showToolWindowCheckbox.addChangeListener(changeListener)
            showTimeInToolbarWidgetCheckbox.addChangeListener(changeListener)
        }
    }

    override fun disposeUIResources() {
        settingsForm = null
    }

    override fun isModified(): Boolean {
        return uiModel != settings
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        settings.loadState(uiModel)
    }

    override fun reset() {
        uiModel.loadState(settings)
        updateUI()
    }

    private fun updateUIModel() {
        if (uiResourcesDisposed()) return
        if (updatingUI) return

        try {
            uiModel.pomodoroDuration = selectedItemAsInteger(settingsForm!!.pomodoroLengthComboBox).minutes
        } catch (e: NumberFormatException) {
            uiModel.pomodoroDuration = Settings.defaultPomodoroDuration
        }

        try {
            uiModel.breakDuration = selectedItemAsInteger(settingsForm!!.breakLengthComboBox).minutes
        } catch (e: NumberFormatException) {
            uiModel.breakDuration = Settings.defaultBreakDuration
        }

        try {
            uiModel.longBreakDuration = selectedItemAsInteger(settingsForm!!.longBreakLengthComboBox).minutes
        } catch (e: NumberFormatException) {
            uiModel.longBreakDuration = Settings.defaultLongBreakDuration
        }

        try {
            uiModel.longBreakFrequency = selectedItemAsInteger(settingsForm!!.longBreakFrequencyComboBox)
        } catch (e: NumberFormatException) {
            uiModel.longBreakFrequency = Settings.defaultLongBreakFrequency
        }

        uiModel.ringVolume = settingsForm!!.ringVolumeSlider.value
        if (lastUIRingVolume != uiModel.ringVolume) {
            lastUIRingVolume = uiModel.ringVolume
            ringSound.play(uiModel.ringVolume)
        }

        uiModel.isPopupEnabled = settingsForm!!.popupCheckBox.isSelected
        uiModel.isBlockDuringBreak = settingsForm!!.blockDuringBreak.isSelected
        uiModel.isShowToolWindow = settingsForm!!.showToolWindowCheckbox.isSelected
        uiModel.isShowTimeInToolbarWidget = settingsForm!!.showTimeInToolbarWidgetCheckbox.isSelected
    }

    private fun updateUI() {
        if (uiResourcesDisposed()) return
        if (updatingUI) return
        updatingUI = true

        settingsForm!!.apply {
            pomodoroLengthComboBox.model.selectedItem = uiModel.pomodoroDuration.minutes.toString()
            breakLengthComboBox.model.selectedItem = uiModel.breakDuration.minutes.toString()
            longBreakLengthComboBox.model.selectedItem = uiModel.longBreakDuration.minutes.toString()
            longBreakFrequencyComboBox.model.selectedItem = uiModel.longBreakFrequency.toString()

            ringVolumeSlider.value = uiModel.ringVolume
            ringVolumeSlider.toolTipText = ringVolumeTooltip(uiModel)

            popupCheckBox.isSelected = uiModel.isPopupEnabled
            blockDuringBreak.isSelected = uiModel.isBlockDuringBreak
            showToolWindowCheckbox.isSelected = uiModel.isShowToolWindow
            showTimeInToolbarWidgetCheckbox.isSelected = uiModel.isShowTimeInToolbarWidget
        }

        updatingUI = false
    }

    private fun uiResourcesDisposed(): Boolean {
        // ActionEvent might occur after disposeUIResources() was invoked
        return settingsForm == null
    }

    @Nls private fun ringVolumeTooltip(uiModel: Settings): String {
        if (uiModel.ringVolume == 0) {
            return UIBundle.message("settings.ringSlider.noSoundTooltip")
        } else {
            return UIBundle.message("settings.ringSlider.volumeTooltip", uiModel.ringVolume)
        }
    }

    @Nls override fun getDisplayName() = UIBundle.message("settings.title")

    override fun getHelpTopic() = null

    override fun getId() = "Pomodoro"

    override fun enableSearch(option: String?) = null

    companion object {
        private const val MIN_TIME_INTERVAL = 1
        private const val MAX_TIME_INTERVAL = 240

        private fun selectedItemAsInteger(comboBox: JComboBox<*>): Int {
            val s = (comboBox.selectedItem as String).trim { it <= ' ' }
            val value = s.toInt()
            if (value < MIN_TIME_INTERVAL) return MIN_TIME_INTERVAL
            if (value > MAX_TIME_INTERVAL) return MAX_TIME_INTERVAL
            return value
        }
    }
}
