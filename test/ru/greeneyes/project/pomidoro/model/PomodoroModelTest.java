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

import org.junit.Test;

import static com.yourkit.util.Asserts.assertTrue;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static ru.greeneyes.project.pomidoro.model.PomodoroModel.PomodoroState.*;

/**
 * User: dima
 * Date: May 29, 2010
 */
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
	public void continueFromSavedState_And_FinishPomodoro() throws InterruptedException {
		long pomodoroStartTime = System.currentTimeMillis() - MILLISECONDS.convert(24, SECONDS);
		PomodoroModelState modelState = new PomodoroModelState(RUN, RUN, pomodoroStartTime);
		PomodoroModel model = new PomodoroModel(settings(MILLISECONDS.convert(25, SECONDS)), modelState);

		assertThat(model.getState(), equalTo(RUN));
		assertTrue(model.getProgress() > 0);
		assertThat(model.getPomodorosAmount(), equalTo(0));

		Thread.sleep(UPDATE_INTERVAL);
		model.updateState();

		assertThat(model.getState(), equalTo(BREAK));
		assertThat(model.getPomodorosAmount(), equalTo(1));
	}

	@Test
	public void doNotContinueFromLastState_IfALotOfTimeHasPassed() throws InterruptedException {
		long pomodoroStartTime = System.currentTimeMillis() - MILLISECONDS.convert(1, HOURS);
		PomodoroModelState modelState = new PomodoroModelState(RUN, RUN, pomodoroStartTime);
		PomodoroModel model = new PomodoroModel(settings(MILLISECONDS.convert(25, SECONDS)), modelState);

		assertThat(model.getState(), equalTo(STOP));
		assertThat(model.getProgress(), equalTo(0));
		assertThat(model.getPomodorosAmount(), equalTo(0));
	}

	@Test
	public void savePomodoroModelState() throws InterruptedException {
		long pomodoroStartTime = System.currentTimeMillis() - MILLISECONDS.convert(24, SECONDS);
		PomodoroModelState modelState = new PomodoroModelState(RUN, RUN, pomodoroStartTime);
		PomodoroModel model = new PomodoroModel(settings(MILLISECONDS.convert(25, SECONDS)), modelState);

		assertThat(model.getState(), equalTo(RUN));

		Thread.sleep(UPDATE_INTERVAL);
		model.updateState();

		assertThat(modelState.getPomodoroState(), equalTo(BREAK));
		assertTrue(modelState.getStartTime() > pomodoroStartTime);
	}

	private static Settings settings(final long testPomodoroLength) {
		return settings(testPomodoroLength, -1);
	}

	private static Settings settings(final long testPomodoroLength, final long testBreakLength) {
		return new Settings() {
			@Override
			public long getPomodoroLength() {
				return testPomodoroLength;
			}

			@Override
			public long getBreakLength() {
				return testBreakLength;
			}
		};
	}
}
