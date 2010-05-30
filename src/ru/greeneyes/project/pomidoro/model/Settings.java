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
}
