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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarCustomComponentFactory;
import org.jetbrains.annotations.NotNull;
import ru.greeneyes.project.pomidoro.model.PomodoroModel;
import ru.greeneyes.project.pomidoro.toolkitwindow.PomodoroPresenter;

import javax.swing.*;

/**
 * User: dima
 * Date: May 29, 2010
 */
public class PomodoroPanelFactory extends StatusBarCustomComponentFactory {
	@Override
	public JComponent createComponent(@NotNull StatusBar statusBar) {
		final JLabel label = new JLabel();

		final PomodoroComponent pomodoroComponent = ApplicationManager.getApplication().getComponent(PomodoroComponent.class);
		final PomodoroModel model = pomodoroComponent.getModel();
		updateLabel(model, label);

		model.addUpdateListener(label, new Runnable() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateLabel(model, label);
					}
				});
			}
		});
		return label;
	}

	private void updateLabel(PomodoroModel model, JLabel label) {
		int timeLeft = model.getProgressMax() - model.getProgress();
		label.setText(PomodoroPresenter.formatTime(timeLeft));
	}
}
