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
 * // TODO
 * <p/>
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

		Object selectedItem = settingsForm.pomodoroLengthComboBox.getSelectedItem();
		try {
			uiModel.pomodoroLength = Integer.valueOf((String) selectedItem);
		} catch (NumberFormatException e) {
			e.printStackTrace(); // TODO
		}

		selectedItem = settingsForm.breakLengthComboBox.getSelectedItem();
		try {
			uiModel.breakLength = Integer.valueOf((String) selectedItem);
		} catch (NumberFormatException e) {
			e.printStackTrace(); // TODO
		}

		uiModel.ringVolume = settingsForm.ringVolumeSlider.getValue();

		uiModel.popupEnabled = settingsForm.popupCheckBox.isSelected();
	}

	private void updateUI() {
		if (updatingUI) return;
		updatingUI = true;

		settingsForm.pomodoroLengthComboBox.getModel().setSelectedItem(String.valueOf(uiModel.pomodoroLength));
		settingsForm.breakLengthComboBox.getModel().setSelectedItem(String.valueOf(uiModel.breakLength));

		settingsForm.ringVolumeSlider.setValue(uiModel.ringVolume);

		settingsForm.popupCheckBox.setSelected(uiModel.popupEnabled);
		settingsForm.popupCheckBox.setText(uiModel.popupEnabled ? "enabled" : "disabled"); // TODO

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
