package ru.greeneyes.project.pomidoro;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * @author ivanalx
 * @date 29.04.2010 13:32:06
 */
public class PomodoroController {
	private PomodoroForm form;

	private volatile PomodoroState state = PomodoroState.STOP;

	private volatile long lastTimeStart;

	private final int runTime;
	private final int breakTime;

	private int donePomodoroAmount = 0;

	public PomodoroController(PomodoroForm form, int runTime, int breakTime) {
		this.form = form;
		this.runTime = runTime;
		this.breakTime = breakTime;
	}

	public void buttonPressed() {
		switch (state) {
			case RUN: {
				moveToStop();
				break;
			}
			case STOP: {
				moveToRun();
				break;
			}
			case BREAK: {
				moveToStop();
				break;
			}
		}
	}

	public void update() {
		long time = System.currentTimeMillis();
		switch (state) {
			case RUN: {
				if (time >= (lastTimeStart + runTime)) {
					updateTimer(lastTimeStart + runTime);
					moveToBreak();
					makeOneMorePomodoroDone();
				} else {
					updateTimer(time);
					updateProgressBarText("Working");
				}

				break;
			}
			case BREAK: {
				if (time >= (lastTimeStart + breakTime)) {
					updateTimer(lastTimeStart + breakTime);
					updateProgressBarText("Break is done");
					moveToStop();
				} else {
					updateTimer(time);
					updateProgressBarText("Break");
				}
				break;
			}
			case STOP: {
				break;
			}
		}
	}

	private void makeOneMorePomodoroDone() {
		donePomodoroAmount += 1;
		invokeAndWait(new Runnable() {
			public void run() {
				form.setPomodoroAmount(donePomodoroAmount);
			}
		});
	}

	private enum PomodoroState {
		STOP,
		RUN,
		BREAK
	}


	private void moveToBreak() {
		lastTimeStart = System.currentTimeMillis();
		invokeAndWait(new Runnable() {
			public void run() {
				makeButtonStop();
				form.getProgressBar().setMaximum(breakTime / 1000);
				form.getProgressBar().setValue(0);
				updateProgressBarText("Break");
				int s = getDonePomodoroAmount() + 1;
				createBalloon("You have done " + s + " Pomodoro" + ((s > 1)? "s":"") + ". Take break.");
			}
		});
		state = PomodoroState.BREAK;
	}

	private void makeButtonStop() {
		form.getControlButton().setText("Stop");
		form.getControlButton().setIcon(new ImageIcon(getClass().getResource("/ru/greeneyes/project/pomidoro/stop-icon.png")));
	}

	private void makeButtonStart() {
		form.getControlButton().setText("Play");
		form.getControlButton().setIcon(new ImageIcon(getClass().getResource("/ru/greeneyes/project/pomidoro/play-icon.png")));
	}


	private void moveToStop() {
		lastTimeStart = 0;
		invokeAndWait(new Runnable() {
			public void run() {
				makeButtonStart();
			}
		});
		state = PomodoroState.STOP;
	}


	private void moveToRun() {
		lastTimeStart = System.currentTimeMillis();
		invokeAndWait(new Runnable() {
			public void run() {
				makeButtonStop();
				form.getProgressBar().setMaximum(runTime / 1000);
				form.getProgressBar().setValue(0);
				updateProgressBarText("Working");
			}
		});
		state = PomodoroState.RUN;
	}

	private void createBalloon(String text) {
		/*
		JRootPane rp = SwingUtilities.getRootPane(form.getRootPanel());
		IdeFrame ideFrame = WindowManager.getInstance().getAllFrames()[0];

		JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(text, MessageType.INFO, null)
					.setFadeoutTime(20000)
					.setBorderColor(Color.WHITE)
					.setCloseButtonEnabled(true)
					.createBalloon().show(null, Balloon.Position.atRight);
					*/
	}

	private void updateProgressBarText(final String prefix) {
		final JProgressBar pb = form.getProgressBar();
		int value = pb.getValue();
		final int min = value / 60;
		final int sec = value % 60;

		invokeAndWait(new Runnable() {
			public void run() {
				pb.setString(prefix + ": " + min + ":" + ((sec < 10)? ("0"+ sec): (sec)));
			}
		});
	}


	private void updateTimer(final long time) {
		invokeAndWait(new Runnable() {
			public void run() {
				JProgressBar pb = form.getProgressBar();
				pb.setValue(pb.getMaximum() - (int) (time - lastTimeStart) / 1000);
			}
		});
	}

	private static void invokeAndWait(Runnable r) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		} else {
			r.run();
		}
	}

	public int getDonePomodoroAmount() {
		return donePomodoroAmount;
	}
}
