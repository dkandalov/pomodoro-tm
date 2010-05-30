package ru.greeneyes.project.pomidoro.model;

/**
* User: dima
* Date: May 30, 2010
*/
public class ControlThread extends Thread {
	private final PomodoroModel model;
	private volatile boolean shouldStop;

	public ControlThread(PomodoroModel model) {
		this.model = model;
		setDaemon(true);
	}

	@Override
	public void run() {
		while (!shouldStop) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			model.updateState();
		}
	}

	public void shouldStop() {
		shouldStop = true;
	}
}
