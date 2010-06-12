package ru.greeneyes.project.pomidoro.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.TestOnly;

/**
 * User: dima
 * Date: Jun 12, 2010
 */
@State(name = "PomodoroState", storages = {@Storage(id = "other", file = "$APP_CONFIG$/pomodoro.state.xml")})
public class PomodoroModelState implements PersistentStateComponent<PomodoroModelState> {
	public PomodoroModel.PomodoroState state;
	public PomodoroModel.PomodoroState lastState;
	public long startTime;
	// TODO pomodoro amount

	public PomodoroModelState() {
		state = PomodoroModel.PomodoroState.STOP;
		lastState = null;
		startTime = 0;
	}

	@TestOnly
	PomodoroModelState(PomodoroModel.PomodoroState state, PomodoroModel.PomodoroState lastState, long startTime) {
		this.state = state;
		this.lastState = lastState;
		this.startTime = startTime;
	}

	@Override
	public PomodoroModelState getState() {
		return this;
	}

	@Override
	public void loadState(PomodoroModelState state) {
		XmlSerializerUtil.copyBean(state, this);
	}
}
