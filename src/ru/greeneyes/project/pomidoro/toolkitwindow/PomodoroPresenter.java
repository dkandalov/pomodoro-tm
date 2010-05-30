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
package ru.greeneyes.project.pomidoro.toolkitwindow;

import ru.greeneyes.project.pomidoro.model.PomodoroModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * User: dima
 * Date: May 29, 2010
 */
public class PomodoroPresenter {
	private final ImageIcon playIcon = new ImageIcon(getClass().getResource("/ru/greeneyes/project/pomidoro/resources/play-icon.png"));
	private final ImageIcon stopIcon = new ImageIcon(getClass().getResource("/ru/greeneyes/project/pomidoro/resources/stop-icon.png"));

	private final PomodoroForm form = new PomodoroForm();
	private final PomodoroModel model;
	private String progressBarPrefix = "";

	public PomodoroPresenter(final PomodoroModel model) {
		this.model = model;

		form.getControlButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				model.switchToNextState();
				updateUI();
			}
		});
		form.getPomodorosLabel().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() >= 2) {
					model.resetPomodoros();
					updateUI();
				}
			}
		});
		updateUI();

		model.addUpdateListener(this, new Runnable() {
			@Override
			public void run() {
				updateUI();
			}
		});
	}

	private void updateUI() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				switch (model.getState()) {
					case RUN:
						form.getControlButton().setText("Stop");
						form.getControlButton().setIcon(stopIcon);
						progressBarPrefix = "Working";
						break;
					case STOP:
						form.getControlButton().setText("Start");
						form.getControlButton().setIcon(playIcon);
						break;
					case BREAK:
						form.getControlButton().setText("Stop");
						progressBarPrefix = "Break";
						break;
					default:
						throw new IllegalStateException();
				}
				form.getPomodorosLabel().setText("Pomodoros: " + model.getPomodorosAmount());

				form.getProgressBar().setMaximum(model.getProgressMax());
				form.getProgressBar().setValue(model.getProgress());

				int timeLeft = model.getProgressMax() - model.getProgress();
				form.getProgressBar().setString(progressBarPrefix + " " + formatTime(timeLeft));
			}
		});
	}

	public static String formatTime(int timeLeft) {
		int min = timeLeft / 60;
		int sec = timeLeft % 60;
		return String.format("%02d", min) + ":" + String.format("%02d", sec);
	}

	public JComponent getContentPane() {
		return form.getRootPanel();
	}

}
