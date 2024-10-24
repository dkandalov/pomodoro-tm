package pomodoro.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import pomodoro.RingSound
import pomodoro.UIBundle
import pomodoro.model.Settings
import pomodoro.model.time.minutes
import java.awt.event.ActionListener
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.event.ChangeListener

class SettingsPresenter(private val settings: Settings = service()): SearchableConfigurable {
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
            showTimeInToolbarWidgetCheckbox.isSelected = uiModel.isShowTimeInToolbarWidget
            startNewPomodoroAfterBreak.isSelected = uiModel.startNewPomodoroAfterBreak
        }

        updatingUI = false
    }

    // ActionEvent might occur after disposeUIResources() was invoked
    private fun uiResourcesDisposed() = settingsForm == null

    private fun ringVolumeTooltip(uiModel: Settings) =
        if (uiModel.ringVolume == 0) {
            UIBundle.message("settings.ringSlider.noSoundTooltip")
        } else {
            UIBundle.message("settings.ringSlider.volumeTooltip", uiModel.ringVolume)
        }

    override fun getDisplayName() = UIBundle.message("settings.title")

    override fun getId() = "Pomodoro"
}

private fun selectedItemAsInteger(comboBox: JComboBox<*>): Int {
    val s = (comboBox.selectedItem as String).trim { it <= ' ' }
    val value = s.toInt()
    val minTimeInterval = 1
    val maxTimeInterval = 240
    return when {
        value < minTimeInterval -> minTimeInterval
        value > maxTimeInterval -> maxTimeInterval
        else                      -> value
    }
}
