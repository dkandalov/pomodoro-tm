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
package ru.greeneyes.project.pomidoro.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import ru.greeneyes.project.pomidoro.UIBundle;
import ru.greeneyes.project.pomidoro.model.Settings;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: dima
 * Date: Oct 18, 2010
 */
public class SettingsPresenter implements Configurable {
	private final Settings settings;
	private SettingsForm settingsForm;
	private Settings uiModel;
	private boolean updatingUI;

	public SettingsPresenter(Settings settings) {
		this.settings = settings;
	}

	@Override
	public JComponent createComponent() {
		settingsForm = new SettingsForm();
		uiModel = new Settings();

		setupUIBindings();

		return settingsForm.getRootPanel();
	}

	private void setupUIBindings() {
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
		settingsForm.popupCheckBox.addChangeListener(changeListener);
		settingsForm.ringVolumeSlider.addChangeListener(changeListener);
	}

	@Override
	public void disposeUIResources() {
		settingsForm = null;
	}

	public boolean isModified() {
		return uiModel.isDifferentFrom(settings);
	}

	@Override
	public void apply() throws ConfigurationException {
		uiModel.saveTo(settings);
	}

	@Override
	public void reset() {
		uiModel.loadFrom(settings);
		updateUI();
	}

	private void updateUIModel() {
		if (updatingUI) return;

		try {
			uiModel.pomodoroLength = selectedItemAsInteger(settingsForm.pomodoroLengthComboBox);
		} catch (NumberFormatException e) {
			uiModel.pomodoroLength = new Settings().pomodoroLength;
		}

		try {
			uiModel.breakLength = selectedItemAsInteger(settingsForm.breakLengthComboBox);
		} catch (NumberFormatException e) {
			uiModel.breakLength = new Settings().breakLength;
		}

		uiModel.ringVolume = settingsForm.ringVolumeSlider.getValue();

		uiModel.popupEnabled = settingsForm.popupCheckBox.isSelected();
	}

	private static Integer selectedItemAsInteger(JComboBox comboBox) {
		String s = ((String) comboBox.getSelectedItem()).trim();
		Integer value = Integer.valueOf(s);
		if (value < 5) return 5;
		return value;
	}

	private void updateUI() {
		if (updatingUI) return;
		updatingUI = true;

		settingsForm.pomodoroLengthComboBox.getModel().setSelectedItem(String.valueOf(uiModel.pomodoroLength));
		settingsForm.breakLengthComboBox.getModel().setSelectedItem(String.valueOf(uiModel.breakLength));

		settingsForm.ringVolumeSlider.setValue(uiModel.ringVolume);

		settingsForm.popupCheckBox.setSelected(uiModel.popupEnabled);

		updatingUI = false;
	}

	@Nls
	@Override
	public String getDisplayName() {
		return UIBundle.message("settings.title");
	}

	@Override
	public Icon getIcon() {
		return null; // TODO ?
	}

	@Override
	public String getHelpTopic() {
		return null;
	}
}
