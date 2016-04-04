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

import org.junit.Test;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static pomodoro.model.PomodoroModel.PomodoroState.BREAK;
import static pomodoro.model.PomodoroModel.PomodoroState.RUN;
import static pomodoro.model.PomodoroModel.PomodoroState.STOP;

public class PomodoroModelTest {
	private static final int UPDATE_INTERVAL = 1100;

	@Test
	public void trackingOnePomodoro() throws InterruptedException {
		PomodoroModel model = new PomodoroModel(settings(2000, -1), new PomodoroModelState());
		assertThat(model.getState(), equalTo(STOP));
		assertThat(model.getProgress(), equalTo(0));
		assertThat(model.getPomodorosAmount(), equalTo(0));

		model.switchToNextState();

		assertThat(model.getState(), equalTo(RUN));
		assertThat(model.getProgressMax(), equalTo(2));

		Thread.sleep(UPDATE_INTERVAL);
		model.updateState();
		assertThat(model.getProgress(), equalTo(1));

		Thread.sleep(UPDATE_INTERVAL);
		model.updateState();
		assertThat(model.getState(), equalTo(BREAK));
		assertThat(model.getProgress(), equalTo(0));
		assertThat(model.getPomodorosAmount(), equalTo(1));
	}

	@Test
	public void stopDuringPomodoro() throws InterruptedException {
		PomodoroModel model = new PomodoroModel(settings(2000, -1), new PomodoroModelState());
		assertThat(model.getState(), equalTo(STOP));
		assertThat(model.getProgress(), equalTo(0));
		assertThat(model.getPomodorosAmount(), equalTo(0));

		model.switchToNextState();

		assertThat(model.getState(), equalTo(RUN));

		Thread.sleep(UPDATE_INTERVAL);
		model.updateState();
		assertThat(model.getProgress(), equalTo(1));

		model.switchToNextState();

		assertThat(model.getState(), equalTo(STOP));
		assertThat(model.getPomodorosAmount(), equalTo(0));
	}

	@Test
	public void autoStopAfterBreak() throws InterruptedException {
		PomodoroModel model = new PomodoroModel(settings(1000, 2000), new PomodoroModelState());

		assertThat(model.getState(), equalTo(STOP));
		model.switchToNextState();

		Thread.sleep(UPDATE_INTERVAL);
		model.updateState();
		assertThat(model.getState(), equalTo(BREAK));

		Thread.sleep(UPDATE_INTERVAL);
		model.updateState();
		assertThat(model.getProgress(), equalTo(1));

		Thread.sleep(UPDATE_INTERVAL);
		model.updateState();
		assertThat(model.getState(), equalTo(STOP));
		assertThat(model.getProgress(), equalTo(2));
	}

	@Test
	public void afterIdeaRestart_ContinueFromSavedState_And_FinishPomodoro() throws InterruptedException {
		long startTime = currentTimeMillis() - SECONDS.toMillis(24);
		PomodoroModelState modelState = new PomodoroModelState(RUN, RUN, startTime);
		PomodoroModel model = new PomodoroModel(settings(SECONDS.toMillis(25)), modelState);

		assertThat(model.getState(), equalTo(RUN));
		assertTrue(model.getProgress() > 0);
		assertThat(model.getPomodorosAmount(), equalTo(0));

		Thread.sleep(UPDATE_INTERVAL);
		model.updateState();

		assertThat(model.getState(), equalTo(BREAK));
		assertThat(model.getPomodorosAmount(), equalTo(1));
	}

	@Test
	public void afterIdeaRestart_ShouldNotContinueFromLastState_IfALotOfTimeHasPassed() throws InterruptedException {
		// last state was RUN
		PomodoroModelState modelState = new PomodoroModelState(RUN, RUN, currentTimeMillis() - HOURS.toMillis(1));
		PomodoroModel model = new PomodoroModel(settings(SECONDS.toMillis(25)), modelState);

		assertThat(model.getState(), equalTo(STOP));
		assertNull(model.getLastState());
		assertThat(model.getProgress(), equalTo(0));
		assertThat(model.getPomodorosAmount(), equalTo(0));

		// last state was BREAK
		modelState = new PomodoroModelState(BREAK, RUN, currentTimeMillis() - HOURS.toMillis(1));
		model = new PomodoroModel(settings(MINUTES.toMillis(6)), modelState);

		assertThat(model.getState(), equalTo(STOP));
		assertNull(model.getLastState());
		assertThat(model.getProgress(), equalTo(0));
		assertThat(model.getPomodorosAmount(), equalTo(0));
	}

	@Test
	public void savePomodoroModelState() throws InterruptedException {
		long pomodoroStartTime = currentTimeMillis() - SECONDS.toMillis(24);
		PomodoroModelState modelState = new PomodoroModelState(RUN, RUN, pomodoroStartTime);
		PomodoroModel model = new PomodoroModel(settings(SECONDS.toMillis(24)), modelState);

		assertThat(model.getState(), equalTo(RUN));

		Thread.sleep(UPDATE_INTERVAL);
		model.updateState();

		assertThat(modelState.getPomodoroState(), equalTo(BREAK));
		assertTrue("model state was updated", modelState.getStartTime() > pomodoroStartTime);
	}

	private static Settings settings(final long testPomodoroLength) {
		return settings(testPomodoroLength, -1);
	}

	private static Settings settings(final long testPomodoroLength, final long testBreakLength) {
		return new Settings() {
			@Override
			public long getPomodoroLengthInMillis() {
				return testPomodoroLength;
			}

			@Override
			public long getBreakLengthInMillis() {
				return testBreakLength;
			}
		};
	}
}
