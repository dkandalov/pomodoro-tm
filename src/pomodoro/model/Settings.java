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
package pomodoro.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

@State(name = "PomodoroSettings", storages = {@Storage(id = "other", file = "$APP_CONFIG$/pomodoro.settings.xml")})
public class Settings implements PersistentStateComponent<Settings> {
	public static final int DEFAULT_POMODORO_LENGTH = 25;
	public static final int DEFAULT_BREAK_LENGTH = 5;
	public static final int DEFAULT_LONG_BREAK_LENGTH = 20;
	public static final int DEFAULT_LONG_BREAK_FREQUENCY = 4;

	public int pomodoroLength = DEFAULT_POMODORO_LENGTH;
	public int breakLength = DEFAULT_BREAK_LENGTH;
	public int longBreakLength = DEFAULT_LONG_BREAK_LENGTH;
	public int longBreakFrequency = DEFAULT_LONG_BREAK_FREQUENCY;
	public int ringVolume = 1;
	public boolean popupEnabled = true;
	public boolean blockDuringBreak = false;
	public boolean showToolWindow = false;
	public boolean showTimeInToolbarWidget = true;

	private long timeoutToContinuePomodoro = MILLISECONDS.convert(DEFAULT_BREAK_LENGTH, MINUTES);
	private final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();


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

	public int getLongBreakLengthInMinutes() {
		return longBreakLength;
	}

	public int getLongBreakFrequency() {
		return longBreakFrequency;
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

	public void setLongBreakLengthInMinutes(int breakLength) {
		this.longBreakLength = breakLength;
	}

	public void setLongBreakFrequency(int longBreakFrequency) {
		this.longBreakFrequency = longBreakFrequency;
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

	public boolean isShowTimeInToolbarWidget() {
		return showTimeInToolbarWidget;
	}

	public void setShowTimeInToolbarWidget(boolean showTimeInToolbarWidget) {
		this.showTimeInToolbarWidget = showTimeInToolbarWidget;
	}

	/**
	 * If IntelliJ shuts down during pomodoro and then restarts, pomodoro can be continued.
	 * This property determines how much time can pass before we consider pomodoro to be expired.
	 *
	 * @return timeout in milliseconds
	 */
	public long getTimeoutToContinuePomodoro() {
		return timeoutToContinuePomodoro;
	}

	public void addChangeListener(ChangeListener changeListener) {
		changeListeners.add(changeListener);
	}

	public void removeChangeListener(ChangeListener changeListener) {
		changeListeners.remove(changeListener);
	}

	@Override
	public Settings getState() {
		return this;
	}

	@Override
	public void loadState(Settings settings) {
		XmlSerializerUtil.copyBean(settings, this);
		for (ChangeListener changeListener : changeListeners) {
			changeListener.onChange(this);
		}
	}

	@SuppressWarnings({"RedundantIfStatement"})
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Settings settings = (Settings) o;

		if (blockDuringBreak != settings.blockDuringBreak) return false;
		if (breakLength != settings.breakLength) return false;
		if (longBreakLength != settings.longBreakLength) return false;
		if (longBreakFrequency != settings.longBreakFrequency) return false;
		if (pomodoroLength != settings.pomodoroLength) return false;
		if (popupEnabled != settings.popupEnabled) return false;
		if (ringVolume != settings.ringVolume) return false;
		if (showToolWindow != settings.showToolWindow) return false;
		if (showTimeInToolbarWidget != settings.showTimeInToolbarWidget) return false;
		if (timeoutToContinuePomodoro != settings.timeoutToContinuePomodoro) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = pomodoroLength;
		result = 31 * result + breakLength;
		result = 31 * result + longBreakLength;
		result = 31 * result + longBreakFrequency;
		result = 31 * result + ringVolume;
		result = 31 * result + (popupEnabled ? 1 : 0);
		result = 31 * result + (blockDuringBreak ? 1 : 0);
		result = 31 * result + (showToolWindow ? 1 : 0);
		result = 31 * result + (showTimeInToolbarWidget ? 1 : 0);
		result = 31 * result + (int) (timeoutToContinuePomodoro ^ (timeoutToContinuePomodoro >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "Settings{" +
				"pomodoroLength=" + pomodoroLength +
				", breakLength=" + breakLength +
				", longBreakLength=" + longBreakLength +
				", longBreakFrequency=" + longBreakFrequency +
				", ringVolume=" + ringVolume +
				", popupEnabled=" + popupEnabled +
				", blockDuringBreak=" + blockDuringBreak +
				", showToolWindow=" + showToolWindow +
				", showTimeInToolbarWidget=" + showTimeInToolbarWidget +
				", timeoutToContinuePomodoro=" + timeoutToContinuePomodoro +
				'}';
	}
}
