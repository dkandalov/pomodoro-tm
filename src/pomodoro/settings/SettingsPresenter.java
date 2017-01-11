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
package pomodoro.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pomodoro.PomodoroComponent;
import pomodoro.RingSound;
import pomodoro.UIBundle;
import pomodoro.model.Settings;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsPresenter implements SearchableConfigurable {
	private static final int MIN_TIME_INTERVAL = 1;
	private static final int MAX_TIME_INTERVAL = 240;

	private final Settings settings;
	private SettingsForm settingsForm;
	private Settings uiModel;
	private boolean updatingUI;
	private RingSound ringSound;
	private int lastUIRingVolume = -1;


	public SettingsPresenter() {
		this(PomodoroComponent.getSettings());
	}

	public SettingsPresenter(Settings settings) {
		this.settings = settings;
		this.ringSound = new RingSound();
	}

	@Override
	public JComponent createComponent() {
		settingsForm = new SettingsForm();
		uiModel = new Settings();

		setupUIBindings();

		return settingsForm.getRootPanel();
	}

	private void setupUIBindings() {
		lastUIRingVolume = uiModel.getRingVolume();
		final ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				updateUIModel();
				updateUI();
			}
		};
		ChangeListener changeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				actionListener.actionPerformed(null);
			}
		};
		settingsForm.pomodoroLengthComboBox.addActionListener(actionListener);
		settingsForm.breakLengthComboBox.addActionListener(actionListener);
		settingsForm.longBreakLengthComboBox.addActionListener(actionListener);
		settingsForm.longBreakFrequencyComboBox.addActionListener(actionListener);
		settingsForm.popupCheckBox.addChangeListener(changeListener);
		settingsForm.blockDuringBreak.addChangeListener(changeListener);
		settingsForm.ringVolumeSlider.addChangeListener(changeListener);
		settingsForm.showToolWindowCheckbox.addChangeListener(changeListener);
		settingsForm.showTimeInToolbarWidgetCheckbox.addChangeListener(changeListener);
	}

	@Override
	public void disposeUIResources() {
		settingsForm = null;
	}

	public boolean isModified() {
		return !uiModel.equals(settings);
	}

	@Override
	public void apply() throws ConfigurationException {
		settings.loadState(uiModel);
	}

	@Override
	public void reset() {
		uiModel.loadState(settings);
		updateUI();
	}

	private void updateUIModel() {
		if (uiResourcesDisposed()) return;
		if (updatingUI) return;

		try {
			uiModel.setPomodoroLengthInMinutes(selectedItemAsInteger(settingsForm.pomodoroLengthComboBox));
		} catch (NumberFormatException e) {
			uiModel.setPomodoroLengthInMinutes(Settings.DEFAULT_POMODORO_LENGTH);
		}

		try {
			uiModel.setBreakLengthInMinutes(selectedItemAsInteger(settingsForm.breakLengthComboBox));
		} catch (NumberFormatException e) {
			uiModel.setBreakLengthInMinutes(Settings.DEFAULT_BREAK_LENGTH);
		}

		try {
			uiModel.setLongBreakLengthInMinutes(selectedItemAsInteger(settingsForm.longBreakLengthComboBox));
		} catch (NumberFormatException e) {
			uiModel.setLongBreakLengthInMinutes(Settings.DEFAULT_LONG_BREAK_LENGTH);
		}

		try {
			uiModel.setLongBreakFrequency(selectedItemAsInteger(settingsForm.longBreakFrequencyComboBox));
		} catch (NumberFormatException e) {
			uiModel.setLongBreakFrequency(Settings.DEFAULT_LONG_BREAK_FREQUENCY);
		}

		uiModel.setRingVolume(settingsForm.ringVolumeSlider.getValue());
		if (lastUIRingVolume != uiModel.getRingVolume()) {
			lastUIRingVolume = uiModel.getRingVolume();
			ringSound.play(uiModel.getRingVolume());
		}

		uiModel.setPopupEnabled(settingsForm.popupCheckBox.isSelected());
		uiModel.setBlockDuringBreak(settingsForm.blockDuringBreak.isSelected());
		uiModel.setShowToolWindow(settingsForm.showToolWindowCheckbox.isSelected());
		uiModel.setShowTimeInToolbarWidget(settingsForm.showTimeInToolbarWidgetCheckbox.isSelected());
	}

	private static Integer selectedItemAsInteger(JComboBox comboBox) {
		String s = ((String) comboBox.getSelectedItem()).trim();
		Integer value = Integer.valueOf(s);
		if (value < MIN_TIME_INTERVAL) return MIN_TIME_INTERVAL;
		if (value > MAX_TIME_INTERVAL) return MAX_TIME_INTERVAL;
		return value;
	}

	private void updateUI() {
		if (uiResourcesDisposed()) return;
		if (updatingUI) return;
		updatingUI = true;

		settingsForm.pomodoroLengthComboBox.getModel().setSelectedItem(String.valueOf(uiModel.getPomodoroLengthInMinutes()));
		settingsForm.breakLengthComboBox.getModel().setSelectedItem(String.valueOf(uiModel.getBreakLengthInMinutes()));
		settingsForm.longBreakLengthComboBox.getModel().setSelectedItem(String.valueOf(uiModel.getLongBreakLengthInMinutes()));
		settingsForm.longBreakFrequencyComboBox.getModel().setSelectedItem(String.valueOf(uiModel.getLongBreakFrequency()));

		settingsForm.ringVolumeSlider.setValue(uiModel.getRingVolume());
		settingsForm.ringVolumeSlider.setToolTipText(ringVolumeTooltip(uiModel));

		settingsForm.popupCheckBox.setSelected(uiModel.isPopupEnabled());
		settingsForm.blockDuringBreak.setSelected(uiModel.isBlockDuringBreak());
		settingsForm.showToolWindowCheckbox.setSelected(uiModel.isShowToolWindow());
		settingsForm.showTimeInToolbarWidgetCheckbox.setSelected(uiModel.isShowTimeInToolbarWidget());

		updatingUI = false;
	}

	private boolean uiResourcesDisposed() {
		// ActionEvent might occur after disposeUIResources() was invoked
		return (settingsForm == null);
	}

	@Nls
	private String ringVolumeTooltip(Settings uiModel) {
		if (uiModel.getRingVolume() == 0) {
			return UIBundle.message("settings.ringSlider.noSoundTooltip");
		} else {
			return UIBundle.message("settings.ringSlider.volumeTooltip", uiModel.getRingVolume());
		}
	}

	@Nls
	@Override
	public String getDisplayName() {
		return UIBundle.message("settings.title");
	}

	@Override
	public String getHelpTopic() {
		return null;
	}

	@NotNull
	@Override
	public String getId() {
		return "Pomodoro";
	}

	@Nullable
	@Override
	public Runnable enableSearch(String option) {
		return null;
	}
}
