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
package ru.greeneyes.project.pomidoro;

import org.junit.Test;
import ru.greeneyes.project.pomidoro.model.Settings;
import ru.greeneyes.project.pomidoro.model.PomodoroModel;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static ru.greeneyes.project.pomidoro.model.PomodoroModel.PomodoroState.RUN;
import static ru.greeneyes.project.pomidoro.model.PomodoroModel.PomodoroState.STOP;
import static ru.greeneyes.project.pomidoro.model.PomodoroModel.PomodoroState.BREAK;

/**
 * User: dima
 * Date: May 29, 2010
 */
public class PomodoroModelTest {
	@Test
	public void trackingOnePomodoro() throws InterruptedException {
		PomodoroModel model = new PomodoroModel(new Settings() {
			@Override
			public long getPomodoroLength() {
				return 2000;
			}
		});
		assertThat(model.getState(), equalTo(STOP));
		assertThat(model.getProgress(), equalTo(0));
		assertThat(model.getPomodorosAmount(), equalTo(0));

		model.switchToNextState();

		assertThat(model.getState(), equalTo(RUN));
		assertThat(model.getProgressMax(), equalTo(2));
		
		Thread.sleep(1100);
		model.updateState();
		assertThat(model.getProgress(), equalTo(1));

		Thread.sleep(1100);
		model.updateState();
		assertThat(model.getState(), equalTo(BREAK));
		assertThat(model.getProgress(), equalTo(2));
		assertThat(model.getPomodorosAmount(), equalTo(1));
	}

	@Test
	public void stopDuringPomodoro() throws InterruptedException {
		PomodoroModel model = new PomodoroModel(new Settings() {
			@Override
			public long getPomodoroLength() {
				return 2000;
			}
		});
		assertThat(model.getState(), equalTo(STOP));
		assertThat(model.getProgress(), equalTo(0));
		assertThat(model.getPomodorosAmount(), equalTo(0));

		model.switchToNextState();

		assertThat(model.getState(), equalTo(RUN));

		Thread.sleep(1100);
		model.updateState();
		assertThat(model.getProgress(), equalTo(1));

		model.switchToNextState();

		assertThat(model.getState(), equalTo(STOP));
		assertThat(model.getPomodorosAmount(), equalTo(0));
	}

	@Test
	public void autoStopAfterBreak() throws InterruptedException {
		PomodoroModel model = new PomodoroModel(new Settings() {
			@Override
			public long getPomodoroLength() {
				return 1000;
			}

			@Override
			public long getBreakLength() {
				return 2000;
			}
		});

		assertThat(model.getState(), equalTo(STOP));
		model.switchToNextState();

		Thread.sleep(1100);
		model.updateState();
		assertThat(model.getState(), equalTo(BREAK));

		Thread.sleep(1100);
		model.updateState();
		assertThat(model.getProgress(), equalTo(1));

		Thread.sleep(1100);
		model.updateState();
		assertThat(model.getState(), equalTo(STOP));
		assertThat(model.getProgress(), equalTo(2));
	}
}
