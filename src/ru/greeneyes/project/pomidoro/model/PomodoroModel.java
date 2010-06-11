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

	private final Settings settings;

	private PomodoroState state;
	private PomodoroState lastState;
	private long startTime;
	private int progressMax;
	private int progress;
	private boolean wasManuallyStopped;
	private final WeakHashMap<Object, Runnable> listeners = new WeakHashMap<Object, Runnable>();

	public PomodoroModel(Settings settings) {
		this(settings, PomodoroState.STOP);
	}

	PomodoroModel(Settings settings, PomodoroState state) {
		this.settings = settings;
		this.state = state;
		updateProgressMax();
		this.progress = progressMax;
	}

	public synchronized void switchToNextState() {
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
				updateProgress(time);
				if (time >= startTime + progressMax) {
					state = PomodoroState.BREAK;
					startTime = time;
					updateProgress(time);
					updateProgressMax();
					settings.setPomodorosAmount(settings.getPomodorosAmount() + 1);
				}
				break;
			case BREAK:
				updateProgress(time);
				if (time >= startTime + progressMax) {
					state = PomodoroState.STOP;
					wasManuallyStopped = false;
				}
				break;
			case STOP:
				if (lastState == PomodoroState.STOP) {
					return;
				}
				break;
		}
		lastState = state;

		for (Runnable listener : listeners.values()) {
			listener.run();
		}
	}

	public synchronized int getProgress() {
		return progress;
	}

	public synchronized int getProgressMax() {
		return progressMax / 1000;
	}

	public synchronized int getPomodorosAmount() {
		return settings.getPomodorosAmount();
	}

	public synchronized void resetPomodoros() {
		settings.setPomodorosAmount(0);
	}

	public synchronized PomodoroState getState() {
		return state;
	}

	public synchronized boolean wasManuallyStopped() {
		return wasManuallyStopped;
	}

	public synchronized void addUpdateListener(Object key, Runnable runnable) {
		listeners.put(key, runnable);
	}

	private void updateProgress(long time) {
		progress = (int) ((time - startTime) / 1000);
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
