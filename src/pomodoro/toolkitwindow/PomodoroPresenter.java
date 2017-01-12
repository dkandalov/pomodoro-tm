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
package pomodoro.toolkitwindow;

import pomodoro.model.PomodoroModel;
import pomodoro.UIBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PomodoroPresenter {
	private final ImageIcon playIcon = new ImageIcon(getClass().getResource("/resources/play-icon.png"));
	private final ImageIcon stopIcon = new ImageIcon(getClass().getResource("/resources/stop-icon.png"));

	private final PomodoroForm form = new PomodoroForm();
	private final PomodoroModel model;
	private String progressBarPrefix = "";

	public PomodoroPresenter(final PomodoroModel model) {
		this.model = model;

		form.getControlButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				model.onUserSwitchToNextState();
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
						form.getControlButton().setText(UIBundle.message("toolwindow.button_stop"));
						form.getControlButton().setIcon(stopIcon);
						progressBarPrefix = UIBundle.message("toolwindow.prefix_working");
						break;
					case STOP:
						form.getControlButton().setText(UIBundle.message("toolwindow.button_start"));
						form.getControlButton().setIcon(playIcon);
						break;
					case BREAK:
						form.getControlButton().setText(UIBundle.message("toolwindow.button_stop"));
						progressBarPrefix = UIBundle.message("toolwindow.button_break");
						break;
					default:
						throw new IllegalStateException();
				}
				form.getPomodorosLabel().setText(String.valueOf(model.getPomodorosAmount()));

				form.getProgressBar().setMaximum(model.getProgressMax());
				form.getProgressBar().setValue(hack_for_jdk1_6_u06__IDEA_9_0_2__winXP(model.getProgress()));

				int timeLeft = model.getProgressMax() - model.getProgress();
				form.getProgressBar().setString(progressBarPrefix + " " + formatTime(timeLeft));
			}
		});
	}

	private static int hack_for_jdk1_6_u06__IDEA_9_0_2__winXP(int progress) {
		// for some reason JProgressBar doesn't display text when progress is too small to be displayed
		return progress < 10 ? 10 : progress;
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
