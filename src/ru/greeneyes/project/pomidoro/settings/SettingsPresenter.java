package ru.greeneyes.project.pomidoro.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import ru.greeneyes.project.pomidoro.UIBundle;
import ru.greeneyes.project.pomidoro.model.Settings;

import javax.swing.*;

/**
 * User: dima
 * Date: Oct 18, 2010
 */
public class SettingsPresenter implements Configurable {
	private final Settings settings;
	private SettingsForm settingsForm;

	public SettingsPresenter(Settings settings) {
		this.settings = settings;
	}

	@Override
	public JComponent createComponent() {
		settingsForm = new SettingsForm();
		updateUI(settings);

		return settingsForm.getRootPanel();
	}

	@Override
	public void disposeUIResources() {
		settingsForm = null;
	}

	@SuppressWarnings({"RedundantIfStatement"})
	public boolean isModified() {
		if (settings.pomodoroLength != settingsForm.pomodoroLengthSlider.getValue()) return true;
		// TODO 
		return false;
	}

	@Override
	public void apply() throws ConfigurationException {
		settings.pomodoroLength = settingsForm.pomodoroLengthSlider.getValue();
	}

	@Override
	public void reset() {
		updateUI(new Settings());
	}

	private void updateUI(Settings settings) {
		settingsForm.pomodoroLengthSlider.setValue(settings.pomodoroLength);
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
