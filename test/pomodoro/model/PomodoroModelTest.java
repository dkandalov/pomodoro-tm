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

import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static pomodoro.model.PomodoroModel.PomodoroState.*;

public class PomodoroModelTest {
	private static final int UPDATE_INTERVAL = 1100;

	@Test public void trackingOnePomodoro() throws InterruptedException {
		PomodoroModel model = new PomodoroModel(settings(2000, -1), new PomodoroModelState());
		assertThat(model.getState(), equalTo(STOP));
		assertThat(model.getProgress(), equalTo(0));
		assertThat(model.getPomodorosAmount(), equalTo(0));

		model.switchToNextState(time(0));

		assertThat(model.getState(), equalTo(RUN));
		assertThat(model.getProgressMax(), equalTo(2));

		model.updateState(time(UPDATE_INTERVAL));
		assertThat(model.getProgress(), equalTo(1));

		model.updateState(time(UPDATE_INTERVAL * 2));
		assertThat(model.getState(), equalTo(BREAK));
		assertThat(model.getProgress(), equalTo(0));
		assertThat(model.getPomodorosAmount(), equalTo(1));
	}

	@Test public void stopDuringPomodoro() throws InterruptedException {
		PomodoroModel model = new PomodoroModel(settings(2000, -1), new PomodoroModelState());
		assertThat(model.getState(), equalTo(STOP));
		assertThat(model.getProgress(), equalTo(0));
		assertThat(model.getPomodorosAmount(), equalTo(0));

		model.switchToNextState(time(0));

		assertThat(model.getState(), equalTo(RUN));

		model.updateState(time(UPDATE_INTERVAL));
		assertThat(model.getProgress(), equalTo(1));

		model.switchToNextState(time(UPDATE_INTERVAL * 2));

		assertThat(model.getState(), equalTo(STOP));
		assertThat(model.getPomodorosAmount(), equalTo(0));
	}

	@Test public void autoStopAfterBreak() throws InterruptedException {
		PomodoroModel model = new PomodoroModel(settings(1000, 2000), new PomodoroModelState());

		assertThat(model.getState(), equalTo(STOP));
		model.switchToNextState(time(0));

		model.updateState(time(UPDATE_INTERVAL));
		assertThat(model.getState(), equalTo(BREAK));

		model.updateState(time(UPDATE_INTERVAL) * 2);
		assertThat(model.getProgress(), equalTo(1));

		model.updateState(time(UPDATE_INTERVAL) * 3);
		assertThat(model.getState(), equalTo(STOP));
		assertThat(model.getProgress(), equalTo(2));
	}

	@Test public void afterIdeaRestart_ContinueFromSavedState_And_FinishPomodoro() throws InterruptedException {
		PomodoroModelState modelState = new PomodoroModelState(RUN, RUN, time(SECONDS.toMillis(-24)));
		PomodoroModel model = new PomodoroModel(settings(SECONDS.toMillis(25)), modelState, time(0));

		assertThat(model.getState(), equalTo(RUN));
		assertTrue(model.getProgress() > 0);
		assertThat(model.getPomodorosAmount(), equalTo(0));

		model.updateState(time(UPDATE_INTERVAL));

		assertThat(model.getState(), equalTo(BREAK));
		assertThat(model.getPomodorosAmount(), equalTo(1));
	}

	@Test public void afterIdeaRestart_ShouldNotContinueFromLastState_IfALotOfTimeHasPassed() throws InterruptedException {
		// last state was RUN
		PomodoroModelState modelState = new PomodoroModelState(RUN, RUN, time(HOURS.toMillis(-1)));
		PomodoroModel model = new PomodoroModel(settings(SECONDS.toMillis(25)), modelState, time(0));

		assertThat(model.getState(), equalTo(STOP));
		assertNull(model.getLastState());
		assertThat(model.getProgress(), equalTo(0));
		assertThat(model.getPomodorosAmount(), equalTo(0));

		// last state was BREAK
		modelState = new PomodoroModelState(BREAK, RUN, time(0) - HOURS.toMillis(1));
		model = new PomodoroModel(settings(MINUTES.toMillis(6)), modelState, time(0));

		assertThat(model.getState(), equalTo(STOP));
		assertNull(model.getLastState());
		assertThat(model.getProgress(), equalTo(0));
		assertThat(model.getPomodorosAmount(), equalTo(0));
	}

	@Test public void savePomodoroModelState() throws InterruptedException {
		long pomodoroStartTime = SECONDS.toMillis(-24);
		PomodoroModelState modelState = new PomodoroModelState(RUN, RUN, pomodoroStartTime);
		PomodoroModel model = new PomodoroModel(settings(SECONDS.toMillis(24)), modelState, time(0));

		assertThat(model.getState(), equalTo(RUN));

		model.updateState(time(UPDATE_INTERVAL));

		assertThat(modelState.getPomodoroState(), equalTo(BREAK));
		assertTrue("model state was updated", modelState.getStartTime() > pomodoroStartTime);
	}

	private static long time(long n) {
		return n;
	}

	private static Settings settings(final long pomodoroLengthMs) {
		return settings(pomodoroLengthMs, -1);
	}

	private static Settings settings(final long pomodoroLengthMs, final long breakLengthMs) {
		return new Settings() {
			@Override public long getPomodoroLengthInMillis() {
				return pomodoroLengthMs;
			}

			@Override public long getBreakLengthInMillis() {
				return breakLengthMs;
			}
		};
	}
}
