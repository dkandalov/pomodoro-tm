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
import org.jetbrains.annotations.TestOnly;

/**
 * Class for persisting pomodoro state.
 * It was not implemented as part of {@link Settings} class
 * because instances of this class cannot be directly changed by user.
 *
 * Thread-safe because it's accessed from {@link ControlThread} and
 * IntelliJ platform thread which persists it.
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
