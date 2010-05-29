package ru.greeneyes.project.pomidoro;

/**
 * User: dima
 * Date: May 29, 2010
 */
public class PomodoroModel {

	public enum PomodoroState {
		STOP,
		RUN,
		BREAK
	}

	private final Config config;

	private PomodoroState state;
	private int pomodorosAmount;
	private long startTime;
	private int progressMax;
	private int progress;
	private boolean wasManuallyStopped;

	public PomodoroModel(Config config) {
		this(config, PomodoroState.STOP);
	}

	PomodoroModel(Config config, PomodoroState state) {
		this.config = config;
		this.state = state;
		pomodorosAmount = 0;
		updateProgressMax();
	}

	public synchronized void onButtonClicked() {
		switch (state) {
			case STOP:
				state = PomodoroState.RUN;
				startTime = System.currentTimeMillis();
				updateProgressMax();
				break;
			case RUN:
				state = PomodoroState.STOP;
				wasManuallyStopped = true;
				break;
			case BREAK:
				state = PomodoroState.STOP;
				wasManuallyStopped = true;
				break;
			default:
				throw new IllegalStateException();
		}
		updateState();
	}

	public synchronized void updateState() {
		long time = System.currentTimeMillis();
		switch (state) {
			case RUN:
				progress = (int) ((time - startTime) / 1000);
				if (time >= startTime + progressMax) {
					state = PomodoroState.BREAK;
					startTime = time;
					updateProgressMax();
					pomodorosAmount++;
					wasManuallyStopped = false;
				}
				break;
			case BREAK:
				progress = (int) ((time - startTime) / 1000);
				if (time >= startTime + progressMax) {
					state = PomodoroState.STOP;
					wasManuallyStopped = false;
				}
				break;
			case STOP:
				break;
		}
	}

	public synchronized int getProgress() {
		return progress;
	}

	public synchronized int getProgressMax() {
		return progressMax / 1000;
	}

	public synchronized int getPomodorosAmount() {
		return pomodorosAmount;
	}

	public synchronized void resetPomodoros() {
		pomodorosAmount = 0;
	}

	public synchronized PomodoroState getState() {
		return state;
	}

	public synchronized boolean wasManuallyStopped() {
		return wasManuallyStopped;
	}

	public synchronized boolean isRingEnabled() {
		return config.isRingEnabled();
	}

	private void updateProgressMax() {
		switch (state) {
			case RUN:
				progressMax = (int) config.getPomodoroLength();
				break;
			case BREAK:
				progressMax = (int) config.getBreakLength();
				break;
			case STOP:
				break;
		}
	}
}
