package pomodoro.settings

import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import org.jetbrains.annotations.Nls
import pomodoro.RingSound
import pomodoro.UIBundle
import pomodoro.model.Settings
import pomodoro.model.time.minutes
import java.awt.event.ActionListener
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.event.ChangeListener

class SettingsPresenter constructor(private val settings: Settings = Settings.instance) : SearchableConfigurable {
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
            ringVolumeSlider.addChangeListener(changeListener)
            showToolWindowCheckbox.addChangeListener(changeListener)
            showTimeInToolbarWidgetCheckbox.addChangeListener(changeListener)
            startNewPomodoroAfterBreak.addChangeListener(changeListener)
        }
    }

    override fun disposeUIResources() {
        settingsForm = null
    }

    override fun isModified() = uiModel != settings

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
        uiModel.isShowToolWindow = settingsForm!!.showToolWindowCheckbox.isSelected
        uiModel.isShowTimeInToolbarWidget = settingsForm!!.showTimeInToolbarWidgetCheckbox.isSelected
        uiModel.startNewPomodoroAfterBreak = settingsForm!!.startNewPomodoroAfterBreak.isSelected
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
            showToolWindowCheckbox.isSelected = uiModel.isShowToolWindow
            showTimeInToolbarWidgetCheckbox.isSelected = uiModel.isShowTimeInToolbarWidget
            startNewPomodoroAfterBreak.isSelected = uiModel.startNewPomodoroAfterBreak
        }

        updatingUI = false
    }

    // ActionEvent might occur after disposeUIResources() was invoked
    private fun uiResourcesDisposed() = settingsForm == null

    @Nls private fun ringVolumeTooltip(uiModel: Settings) =
        if (uiModel.ringVolume == 0) {
            UIBundle.message("settings.ringSlider.noSoundTooltip")
        } else {
            UIBundle.message("settings.ringSlider.volumeTooltip", uiModel.ringVolume)
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
            return if (value < MIN_TIME_INTERVAL) MIN_TIME_INTERVAL
            else if (value > MAX_TIME_INTERVAL) MAX_TIME_INTERVAL
            else value
        }
    }
}
