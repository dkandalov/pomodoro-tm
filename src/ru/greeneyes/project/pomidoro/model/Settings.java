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

import java.util.concurrent.TimeUnit;

/**
 * User: dima
 * Date: May 29, 2010
 */
@State(name = "PomodoroSettings", storages = {@Storage(id = "other", file = "$APP_CONFIG$/pomodoro.settings.xml")})
public class Settings implements PersistentStateComponent<Settings> {
	public int pomodoroLength = 25;
	public int breakLength = 5;
	public boolean ringEnabled = true;
	public boolean popupEnabled = true;
	public int pomodorosAmount = 0;

	@Override
	public void loadState(Settings settings) {
		XmlSerializerUtil.copyBean(settings, this);
	}

	@Override
	public Settings getState() {
		return this;
	}

	public long getPomodoroLength() {
//		return 5000;
		return TimeUnit.MILLISECONDS.convert(pomodoroLength, TimeUnit.MINUTES);
	}

	public long getBreakLength() {
//		return 5000;
		return TimeUnit.MILLISECONDS.convert(breakLength, TimeUnit.MINUTES);
	}

	public boolean isRingEnabled() {
		return ringEnabled;
	}

	public boolean isPopupEnabled() {
		return popupEnabled;
	}

	public int getPomodorosAmount() {
		return pomodorosAmount;
	}

	public void setPomodorosAmount(int pomodorosAmount) {
		this.pomodorosAmount = pomodorosAmount;
	}
}
