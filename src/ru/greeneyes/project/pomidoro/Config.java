package ru.greeneyes.project.pomidoro;

import java.util.concurrent.TimeUnit;

/**
 * TODO use intellij persistence for configuration; in this case it will be configurable at least from xml
 *
 * User: dima
 * Date: May 29, 2010
 */
public class Config {
	public long getPomodoroLength() {
		return TimeUnit.MILLISECONDS.convert(25, TimeUnit.MINUTES);
	}

	public long getBreakLength() {
		return TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);
	}
}
