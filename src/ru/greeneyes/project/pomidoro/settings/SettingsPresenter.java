package ru.greeneyes.project.pomidoro.settings;

import ru.greeneyes.project.pomidoro.model.Settings;

import javax.swing.*;

/**
 * User: dima
 * Date: Oct 18, 2010
 */
public class SettingsPresenter {
	private SettingsForm settingsForm = new SettingsForm();
	private final Settings settings;

	public SettingsPresenter(Settings settings) {
		this.settings = settings;

		updateUI(settings);
	}

	public JComponent getContentPane() {
		return settingsForm.getRootPanel();
	}

	public void applyChanges() {
		settings.pomodoroLength = settingsForm.pomodoroLengthSlider.getValue();
	}

	@SuppressWarnings({"RedundantIfStatement"})
	public boolean isModified() {
		if (settings.pomodoroLength != settingsForm.pomodoroLengthSlider.getValue()) return true;
		return false;
	}

	public void reset() {
		updateUI(new Settings());
	}

	public void disposeUIResources() {
		settingsForm = null;
	}

	private void updateUI(Settings settings) {
		settingsForm.pomodoroLengthSlider.setValue(settings.pomodoroLength);
	}
}
