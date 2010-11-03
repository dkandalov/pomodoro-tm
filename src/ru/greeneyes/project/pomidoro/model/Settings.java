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
	public int pomodoroLength = 25;
	public int breakLength = 5;
	public int ringVolume = 1;
	public boolean popupEnabled = true;
	private long timeoutToContinuePomodoro = MILLISECONDS.convert(5, MINUTES);

	public long getPomodoroLength() {
//		return 10000;
		return MILLISECONDS.convert(pomodoroLength, MINUTES);
	}

	public long getBreakLength() {
//		return 5000;
		return MILLISECONDS.convert(breakLength, MINUTES);
	}

	public boolean isRingEnabled() {
		return ringVolume > 0;
	}

	public boolean isPopupEnabled() {
		return popupEnabled;
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
	}

	public void saveTo(Settings settings) {
		settings.pomodoroLength = pomodoroLength;
		settings.breakLength = breakLength;
		settings.ringVolume = ringVolume;
		settings.popupEnabled = popupEnabled;
	}

	@SuppressWarnings({"RedundantIfStatement"})
	public boolean isDifferentFrom(Settings settings) {
		if (pomodoroLength != settings.pomodoroLength) return true;
		if (breakLength != settings.breakLength) return true;
		if (ringVolume != settings.ringVolume) return true;
		if (popupEnabled != settings.popupEnabled) return true;

		return false;
	}

	@Override
	public String toString() {
		return "Settings{" +
				"pomodoroLength=" + pomodoroLength +
				", breakLength=" + breakLength +
				", ringVolume=" + ringVolume +
				", popupEnabled=" + popupEnabled +
				", timeoutToContinuePomodoro=" + timeoutToContinuePomodoro +
				'}';
	}
}
