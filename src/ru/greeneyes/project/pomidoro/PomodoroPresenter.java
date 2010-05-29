package ru.greeneyes.project.pomidoro;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static ru.greeneyes.project.pomidoro.PomodoroModel.PomodoroState.BREAK;

/**
 * User: dima
 * Date: May 29, 2010
 */
public class PomodoroPresenter {
	private final PomodoroForm form = new PomodoroForm();
	private final PomodoroModel model;
	private String progressBarPrefix = "";
	private PomodoroModel.PomodoroState lastState;

	public PomodoroPresenter(final PomodoroModel model) {
		this.model = model;

		form.getControlButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				model.onButtonClicked();
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

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				//noinspection InfiniteLoopStatement
				while (true) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					model.updateState();
					updateUI();
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private void updateUI() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				switch (model.getState()) {
					case RUN:
						form.getControlButton().setText("Stop");
						form.getControlButton().setIcon(new ImageIcon(getClass().getResource("/ru/greeneyes/project/pomidoro/stop-icon.png")));
						progressBarPrefix = "Working";
						break;
					case STOP:
						form.getControlButton().setText("Start");
						form.getControlButton().setIcon(new ImageIcon(getClass().getResource("/ru/greeneyes/project/pomidoro/play-icon.png")));
						if (lastState == BREAK && !model.wasManuallyStopped()) {
							playRingingSound();
						}
						break;
					case BREAK:
						form.getControlButton().setText("Stop");
						progressBarPrefix = "Break";
						playRingingSound();
						break;
					default:
						throw new IllegalStateException();
				}
				lastState = model.getState();
				form.getPomodorosLabel().setText("Pomodoros: " + model.getPomodorosAmount());

				form.getProgressBar().setMaximum(model.getProgressMax());
				form.getProgressBar().setValue(model.getProgress());
				int min = model.getProgress() / 60;
				int sec = model.getProgress() % 60;
				form.getProgressBar().setString(progressBarPrefix + ": " + min + ":" + ((sec < 10) ? ("0" + sec) : (sec)));
			}
		});
	}

	public JComponent getContentPane() {
		return form.getRootPanel();
	}

	private void playRingingSound() {
		// TODO
//		AudioClip audioClip = Applet.newAudioClip();
//		audioClip.play();
	}
}
