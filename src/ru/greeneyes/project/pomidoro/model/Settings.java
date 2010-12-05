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
	public boolean showToolWindow = true;

	private long timeoutToContinuePomodoro = MILLISECONDS.convert(DEFAULT_BREAK_LENGTH, MINUTES);
	private ChangeListener changeListener;

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

	public boolean isShowToolWindow() {
		return showToolWindow;
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

	public void setShowToolWindow(boolean showToolWindow) {
		this.showToolWindow = showToolWindow;
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

	public void setChangeListener(ChangeListener changeListener) {
		this.changeListener = changeListener;
	}

	@Override
	public Settings getState() {
		return this;
	}

	@Override
	public void loadState(Settings settings) {
		XmlSerializerUtil.copyBean(settings, this);
		if (changeListener != null) changeListener.onChange(this);
	}

	@SuppressWarnings({"RedundantIfStatement"})
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Settings settings = (Settings) o;

		if (blockDuringBreak != settings.blockDuringBreak) return false;
		if (breakLength != settings.breakLength) return false;
		if (pomodoroLength != settings.pomodoroLength) return false;
		if (popupEnabled != settings.popupEnabled) return false;
		if (ringVolume != settings.ringVolume) return false;
		if (showToolWindow != settings.showToolWindow) return false;
		if (timeoutToContinuePomodoro != settings.timeoutToContinuePomodoro) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = pomodoroLength;
		result = 31 * result + breakLength;
		result = 31 * result + ringVolume;
		result = 31 * result + (popupEnabled ? 1 : 0);
		result = 31 * result + (blockDuringBreak ? 1 : 0);
		result = 31 * result + (showToolWindow ? 1 : 0);
		result = 31 * result + (int) (timeoutToContinuePomodoro ^ (timeoutToContinuePomodoro >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "Settings{" +
				"pomodoroLength=" + pomodoroLength +
				", breakLength=" + breakLength +
				", ringVolume=" + ringVolume +
				", popupEnabled=" + popupEnabled +
				", blockDuringBreak=" + blockDuringBreak +
				", showToolWindow=" + showToolWindow +
				", timeoutToContinuePomodoro=" + timeoutToContinuePomodoro +
				'}';
	}
}
