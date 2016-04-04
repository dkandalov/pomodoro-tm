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
package pomodoro.toolkitwindow;

import pomodoro.model.PomodoroModelState;
import pomodoro.model.ControlThread;
import pomodoro.model.PomodoroModel;
import pomodoro.model.Settings;

import javax.swing.*;

public class PomodoroForm_Playground {
	public static void main(String[] args) {
		MySettings config = new MySettings();
		PomodoroModel model = new PomodoroModel(config, new PomodoroModelState());
		PomodoroPresenter presenter = new PomodoroPresenter(model);
		new ControlThread(model).start();

		JFrame jFrame = new JFrame();
		jFrame.setContentPane(presenter.getContentPane());
		jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		jFrame.pack();
		jFrame.setVisible(true);
	}

	private static class MySettings extends Settings {
		@Override
		public long getPomodoroLengthInMillis() {
			// timeout should be > 10 to progress in UI
			// because of PomodoroPresenter.hack_for_jdk1_6_u06__IDEA_9_0_2__winXP()
			return 15 * 1000;
		}

		@Override
		public long getBreakLengthInMillis() {
			return 15 * 1000;
		}
	}
}
