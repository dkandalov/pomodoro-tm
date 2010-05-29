package ru.greeneyes.project.pomidoro;

import javax.swing.*;
import java.applet.Applet;
import java.applet.AudioClip;
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
	private final ImageIcon playIcon = new ImageIcon(getClass().getResource("/ru/greeneyes/project/pomidoro/resources/play-icon.png"));
	private final ImageIcon stopIcon = new ImageIcon(getClass().getResource("/ru/greeneyes/project/pomidoro/resources/stop-icon.png"));
	private final AudioClip ringSound = Applet.newAudioClip(getClass().getResource("/ru/greeneyes/project/pomidoro/resources/ring.wav"));

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
						form.getControlButton().setIcon(stopIcon);
						progressBarPrefix = "Working";
						break;
					case STOP:
						form.getControlButton().setText("Start");
						form.getControlButton().setIcon(playIcon);

						if (model.isRingEnabled() && lastState == BREAK && !model.wasManuallyStopped()) {
							ringSound.play();
						}
						break;
					case BREAK:
						form.getControlButton().setText("Stop");
						progressBarPrefix = "Break";
						if (model.isRingEnabled() && lastState != BREAK) {
							ringSound.play();
						}
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

}