package ru.greeneyes.project.pomidoro;

/**
 * @author ivanalx
 * @date 28.04.2010 18:37:08
 */
@Deprecated
public class PomodoroControlThread implements Runnable {
	private PomodoroController controller;

	public PomodoroControlThread(PomodoroController controller) {
		this.controller = controller;
	}

	public void run() {
		while (!Thread.interrupted()) {
			controller.update();
			sleep100ms();
		}
	}

	private void sleep100ms() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
