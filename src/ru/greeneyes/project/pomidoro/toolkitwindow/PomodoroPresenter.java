package ru.greeneyes.project.pomidoro.toolkitwindow;

import ru.greeneyes.project.pomidoro.model.Config;
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
	private final Config config;
	private String progressBarPrefix = "";

	public PomodoroPresenter(final PomodoroModel model, Config config) {
		this.model = model;
		this.config = config;

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
				form.getProgressBar().setString(progressBarPrefix + ": " + formatTime(model.getProgress()));
			}
		});
	}

	public static String formatTime(int progress) {
		int min = progress / 60;
		int sec = progress % 60;
		return String.format("%02d", min) + ":" + String.format("%02d", sec);
	}

	public JComponent getContentPane() {
		return form.getRootPanel();
	}

}
