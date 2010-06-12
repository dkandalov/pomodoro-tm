package ru.greeneyes.project.pomidoro.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.TestOnly;

/**
 * Class for persisting pomodoro state. This is different from {@link ru.greeneyes.project.pomidoro.model.Settings}
 * because its state cannot be directly changed by user.
 *
 * Thread-safe because it's accessed by {@link ru.greeneyes.project.pomidoro.model.ControlThread} and
 * some other thread from IntelliJ platform which persists it.
 *
 * User: dima
 * Date: Jun 12, 2010
 */
@State(name = "PomodoroState", storages = {@Storage(id = "other", file = "$APP_CONFIG$/pomodoro.state.xml")})
public class PomodoroModelState implements PersistentStateComponent<PomodoroModelState> {
	private PomodoroModel.PomodoroState pomodoroState;
	private PomodoroModel.PomodoroState lastState;
	private long startTime;
	private int pomodorosAmount;

	public PomodoroModelState() {
		setPomodoroState(PomodoroModel.PomodoroState.STOP);
		setLastState(null);
		setStartTime(0);
		setPomodorosAmount(0);
	}

	@TestOnly
	PomodoroModelState(PomodoroModel.PomodoroState pomodoroState, PomodoroModel.PomodoroState lastState, long startTime) {
		this.setPomodoroState(pomodoroState);
		this.setLastState(lastState);
		this.setStartTime(startTime);
	}

	@Override
	public PomodoroModelState getState() {
		return this;
	}

	@Override
	public synchronized void loadState(PomodoroModelState state) {
		XmlSerializerUtil.copyBean(state, this);
	}

	public synchronized PomodoroModel.PomodoroState getPomodoroState() {
		return pomodoroState;
	}

	public synchronized void setPomodoroState(PomodoroModel.PomodoroState pomodoroState) {
		this.pomodoroState = pomodoroState;
	}

	public synchronized PomodoroModel.PomodoroState getLastState() {
		return lastState;
	}

	public synchronized void setLastState(PomodoroModel.PomodoroState lastState) {
		this.lastState = lastState;
	}

	public synchronized long getStartTime() {
		return startTime;
	}

	public synchronized void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public synchronized int getPomodorosAmount() {
		return pomodorosAmount;
	}

	public synchronized void setPomodorosAmount(int pomodorosAmount) {
		this.pomodorosAmount = pomodorosAmount;
	}
}
