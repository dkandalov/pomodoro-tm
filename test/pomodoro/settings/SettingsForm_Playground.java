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
import pomodoro.model.Settings;

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
		settings.setPomodoroLengthInMinutes(123);
		return settings;
	}

	private static Button createButton(String label, ActionListener actionListener) {
		Button button = new Button(label);
		button.addActionListener(actionListener);
		return button;
	}
}
