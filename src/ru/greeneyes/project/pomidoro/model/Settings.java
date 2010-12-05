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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Stores pomodoro plugin settings (see fields for details).
 * The state of this class is persisted by IntelliJ.
 * This is class must be registered in plugin.xml.
 * <p/><p/>
 * User: dima
 * Date: May 29, 2010
 */
@State(name = "PomodoroSettings", storages = {@Storage(id = "other", file = "$APP_CONFIG$/pomodoro.settings.xml")})
public class Settings implements PersistentStateComponent<Settings> {
	public static final int DEFAULT_POMODORO_LENGTH = 25;
	public static final int DEFAULT_BREAK_LENGTH = 5;

	public int pomodoroLength = DEFAULT_POMODORO_LENGTH;
	public int breakLength = DEFAULT_BREAK_LENGTH;
	public int ringVolume = 1;
	public boolean popupEnabled = true;
	public boolean blockDuringBreak = false;
	
	private long timeoutToContinuePomodoro = MILLISECONDS.convert(DEFAULT_BREAK_LENGTH, MINUTES);

	public long getPomodoroLengthInMillis() {
//		return 10000;
		return MINUTES.toMillis(pomodoroLength);
	}

	public long getBreakLengthInMillis() {
//		return 5000;
		return MINUTES.toMillis(breakLength);
	}

	public int getPomodoroLengthInMinutes() {
		return pomodoroLength;
	}

	public int getBreakLengthInMinutes() {
		return breakLength;
	}

	public int getRingVolume() {
		return ringVolume;
	}

	public boolean isPopupEnabled() {
		return popupEnabled;
	}

	public boolean isBlockDuringBreak() {
		return blockDuringBreak;
	}

	public void setPomodoroLengthInMinutes(int pomodoroLength) {
		this.pomodoroLength = pomodoroLength;
	}

	public void setBreakLengthInMinutes(int breakLength) {
		this.breakLength = breakLength;
	}

	public void setRingVolume(int ringVolume) {
		this.ringVolume = ringVolume;
	}

	public void setPopupEnabled(boolean popupEnabled) {
		this.popupEnabled = popupEnabled;
	}

	public void setBlockDuringBreak(boolean blockDuringBreak) {
		this.blockDuringBreak = blockDuringBreak;
	}

	/**
	 * If IntelliJ is shutdown during pomodoro and then restarted, pomodoro can be continued.
	 * This property determines how much time can pass before we consider pomodoro to be expired.
	 *
	 * @return timeout in milliseconds
	 */
	public long getTimeoutToContinuePomodoro() {
		return timeoutToContinuePomodoro;
	}

	@Override
	public void loadState(Settings settings) {
		XmlSerializerUtil.copyBean(settings, this);
	}

	@Override
	public Settings getState() {
		return this;
	}

	public void loadFrom(Settings settings) {
		pomodoroLength = settings.pomodoroLength;
		breakLength = settings.breakLength;
		ringVolume = settings.ringVolume;
		popupEnabled = settings.popupEnabled;
		blockDuringBreak = settings.blockDuringBreak;
	}

	public void saveTo(Settings settings) {
		settings.pomodoroLength = pomodoroLength;
		settings.breakLength = breakLength;
		settings.ringVolume = ringVolume;
		settings.popupEnabled = popupEnabled;
		settings.blockDuringBreak = blockDuringBreak;
	}

	@SuppressWarnings({"RedundantIfStatement"})
	public boolean isDifferentFrom(Settings settings) {
		if (pomodoroLength != settings.pomodoroLength) return true;
		if (breakLength != settings.breakLength) return true;
		if (ringVolume != settings.ringVolume) return true;
		if (popupEnabled != settings.popupEnabled) return true;
		if (blockDuringBreak != settings.blockDuringBreak) return true;

		return false;
	}

	@Override
	public String toString() {
		return "Settings{" +
				"popupEnabled=" + popupEnabled +
				", pomodoroLength=" + pomodoroLength +
				", breakLength=" + breakLength +
				", ringVolume=" + ringVolume +
				", blockDuringBreak=" + blockDuringBreak +
				", timeoutToContinuePomodoro=" + timeoutToContinuePomodoro +
				'}';
	}
}
