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
package ru.greeneyes.project.pomidoro.model;

import java.util.WeakHashMap;

import static ru.greeneyes.project.pomidoro.model.PomodoroModel.PomodoroState.BREAK;
import static ru.greeneyes.project.pomidoro.model.PomodoroModel.PomodoroState.RUN;
import static ru.greeneyes.project.pomidoro.model.PomodoroModel.PomodoroState.STOP;

/**
 * User: dima
 * Date: May 29, 2010
 */
public class PomodoroModel {
	private static final int PROGRESS_INTERVAL_MILLIS = 1000;

	public enum PomodoroState {
		STOP,
		RUN,
		BREAK
	}

	private final Settings settings;

	private PomodoroState state;
	private PomodoroState lastState;
	private long startTime;
	private int progressMax;
	private int progress;
	private int pomodorosAmount;
	private boolean wasManuallyStopped;
	private final WeakHashMap<Object, Runnable> listeners = new WeakHashMap<Object, Runnable>();

	private final PomodoroModelState pomodoroModelState;

	public PomodoroModel(Settings settings, PomodoroModelState pomodoroModelState) {
		this.settings = settings;
		this.pomodoroModelState = pomodoroModelState;

		loadModelState();

		updateProgressMax();
		progress = progressMax;
	}

	public synchronized void switchToNextState() {
		switch (state) {
			case STOP:
				state = RUN;
				startTime = System.currentTimeMillis();
				updateProgressMax();
				break;
			case RUN:
				state = STOP;
				wasManuallyStopped = true;
				break;
			case BREAK:
				state = STOP;
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
				updateProgress(time);
				if (time >= startTime + progressMax) {
					state = BREAK;
					startTime = time;
					updateProgress(time);
					updateProgressMax();
					pomodorosAmount++;
				}
				break;
			case BREAK:
				updateProgress(time);
				if (time >= startTime + progressMax) {
					state = STOP;
					wasManuallyStopped = false;
				}
				break;
			case STOP:
				if (lastState == STOP) {
					return;
				}
				break;
		}

		for (Runnable listener : listeners.values()) {
			listener.run();
		}

		if (lastState != state) {
			lastState = state;
			saveModelState();
		}
		lastState = state;
	}

	public synchronized int getProgress() {
		return progress;
	}

	public synchronized int getProgressMax() {
		return progressMax / PROGRESS_INTERVAL_MILLIS;
	}

	public synchronized int getPomodorosAmount() {
		return pomodorosAmount;
	}

	public synchronized void resetPomodoros() {
		pomodorosAmount = 0;
		saveModelState();
	}

	public synchronized PomodoroState getState() {
		return state;
	}

	public synchronized PomodoroState getLastState() {
		return lastState;
	}

	public synchronized boolean wasManuallyStopped() {
		return wasManuallyStopped;
	}

	public synchronized void addUpdateListener(Object key, Runnable runnable) {
		listeners.put(key, runnable);
	}

	private void loadModelState() {
		state = pomodoroModelState.getPomodoroState();
		lastState = pomodoroModelState.getLastState();
		startTime = pomodoroModelState.getStartTime();
		pomodorosAmount = pomodoroModelState.getPomodorosAmount();

		if (pomodoroModelState.getPomodoroState() == RUN) {
			long timeSincePomodoroStart = System.currentTimeMillis() - pomodoroModelState.getStartTime();
			boolean shouldNotContinuePomodoro = (timeSincePomodoroStart > settings.getTimeoutToContinuePomodoro());
			if (shouldNotContinuePomodoro) {
				state = STOP;
				lastState = null;
				startTime = -1;
				saveModelState();
			}
		}
	}

	private void saveModelState() {
		pomodoroModelState.setPomodoroState(state);
		pomodoroModelState.setLastState(lastState);
		pomodoroModelState.setStartTime(startTime);
		pomodoroModelState.setPomodorosAmount(pomodorosAmount);
	}

	private void updateProgress(long time) {
		progress = (int) ((time - startTime) / PROGRESS_INTERVAL_MILLIS);
		if (progress > getProgressMax()) {
			progress = getProgressMax();
		}
	}

	private void updateProgressMax() {
		switch (state) {
			case RUN:
				progressMax = (int) settings.getPomodoroLength();
				break;
			case BREAK:
				progressMax = (int) settings.getBreakLength();
				break;
		}
	}
}
