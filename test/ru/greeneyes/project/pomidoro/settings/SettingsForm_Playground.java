package ru.greeneyes.project.pomidoro.settings;

import com.intellij.openapi.options.ConfigurationException;
import ru.greeneyes.project.pomidoro.model.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: dima
 * Date: Nov 3, 2010
 */
public class SettingsForm_Playground {
	public static void main(String[] args) {
		final Settings settings = createSettings();
		final SettingsPresenter presenter = new SettingsPresenter(settings);
		JComponent component = presenter.createComponent();
		presenter.reset();

		JFrame jFrame = new JFrame();
		jFrame.setContentPane(component);

		// add additional buttons for manual testing
		jFrame.getContentPane().setLayout(new FlowLayout());
		jFrame.getContentPane().add(createButton("<Apply>", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					System.out.println("modified: " + presenter.isModified());
					System.out.println(settings);
					presenter.apply();
					System.out.println(settings);
				} catch (ConfigurationException e1) {
					e1.printStackTrace();
				}
			}
		}));
		jFrame.getContentPane().add(createButton("<Reset>", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("modified: " + presenter.isModified());
				System.out.println(settings);
				presenter.reset();
				System.out.println(settings);
			}
		}));

		jFrame.setTitle(presenter.getDisplayName());
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.pack();
		jFrame.setVisible(true);
	}

	private static Settings createSettings() {
		final Settings settings = new Settings();
		settings.pomodoroLength = 123;
		return settings;
	}

	private static Button createButton(String label, ActionListener actionListener) {
		Button button = new Button(label);
		button.addActionListener(actionListener);
		return button;
	}
}
