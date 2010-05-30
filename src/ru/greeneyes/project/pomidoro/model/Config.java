package ru.greeneyes.project.pomidoro.model;

/**
 * TODO use intellij persistence for configuration; in this case plugin will be configurable at least from xml
 *
 * User: dima
 * Date: May 29, 2010
 */
public class Config {
	public long getPomodoroLength() {
		return 5000;
//		return TimeUnit.MILLISECONDS.convert(25, TimeUnit.MINUTES);
	}

	public long getBreakLength() {
		return 5000;
//		return TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);
	}

	public boolean isRingEnabled() {
		return false;
	}

	public boolean isPopupEnabled() {
		return true;
	}
}
