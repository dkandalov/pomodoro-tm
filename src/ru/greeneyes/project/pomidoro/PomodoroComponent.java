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

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import ru.greeneyes.project.pomidoro.model.ControlThread;
import ru.greeneyes.project.pomidoro.model.PomodoroModel;
import ru.greeneyes.project.pomidoro.model.PomodoroModelState;
import ru.greeneyes.project.pomidoro.model.Settings;
import ru.greeneyes.project.pomidoro.settings.SettingsPresenter;
import ru.greeneyes.project.pomidoro.toolkitwindow.PomodoroToolkitWindow;

import javax.swing.*;
import java.applet.Applet;
import java.applet.AudioClip;

import static ru.greeneyes.project.pomidoro.model.PomodoroModel.PomodoroState.BREAK;

/**
 * User: dima
 * Date: May 30, 2010
 */
public class PomodoroComponent implements ApplicationComponent, Configurable {
	private static final String POMODORO = "Pomodoro";

	private ControlThread controlThread;
	private PomodoroModel model;
	private SettingsPresenter settingsPresenter;

	@Override
	public void initComponent() {
		Settings settings = ServiceManager.getService(Settings.class);
		settingsPresenter = new SettingsPresenter(settings);

		model = new PomodoroModel(settings, ServiceManager.getService(PomodoroModelState.class));

		new UserNotifier(settings, model);

		controlThread = new ControlThread(model);
		controlThread.start();
	}

	@Override
	public void disposeComponent() {
		controlThread.shouldStop();
	}

	@NotNull
	@Override
	public String getComponentName() {
		return POMODORO;
	}

	public PomodoroModel getModel() {
		return model;
	}

	@Nls
	@Override
	public String getDisplayName() {
		return settingsPresenter.getDisplayName();
	}

	@Override
	public Icon getIcon() {
		return settingsPresenter.getIcon();
	}

	@Override
	public String getHelpTopic() {
		return settingsPresenter.getHelpTopic();
	}

	@Override
	public JComponent createComponent() {
		return settingsPresenter.createComponent();
	}

	@Override
	public boolean isModified() {
		return settingsPresenter.isModified();
	}

	@Override
	public void apply() throws ConfigurationException {
		settingsPresenter.apply();
	}

	@Override
	public void reset() {
		settingsPresenter.reset();
	}

	@Override
	public void disposeUIResources() {
		settingsPresenter.disposeUIResources();
	}

	private static class UserNotifier {
		private final AudioClip ringSound1 = Applet.newAudioClip(getClass().getResource("/resources/ring.wav"));
		private final AudioClip ringSound2 = Applet.newAudioClip(getClass().getResource("/resources/ring2.wav"));
		private final AudioClip ringSound3 = Applet.newAudioClip(getClass().getResource("/resources/ring3.wav"));

		public UserNotifier(final Settings settings, final PomodoroModel model) {
			model.addUpdateListener(this, new Runnable() {
				@Override
				public void run() {
					switch (model.getState()) {
						case STOP:
							if (model.getLastState() == BREAK && !model.wasManuallyStopped()) {
								if (settings.isRingEnabled()) playRingSound(settings.ringVolume);
							}
							break;
						case BREAK:
							if (model.getLastState() != BREAK) {
								if (settings.isRingEnabled()) playRingSound(settings.ringVolume);
								if (settings.isPopupEnabled()) showPopupNotification();
							}
							break;
					}
				}
			});
		}

		private void playRingSound(int ringVolume) {
			switch (ringVolume) {
				case 0:
					break;
				case 1:
					ringSound1.play();
					break;
				case 2:
					ringSound2.play();
					break;
				case 3:
					ringSound3.play();
					break;
				default:
					throw new IllegalStateException();
			}
		}

		private void showPopupNotification() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					DataContext dataContext = DataManager.getInstance().getDataContext();
					Project project = PlatformDataKeys.PROJECT.getData(dataContext);
					if (project == null) return;

					String statusMessage = UIBundle.message("notification.text");
					ToolWindowManager.getInstance(project).
							notifyByBalloon(PomodoroToolkitWindow.TOOL_WINDOW_ID, MessageType.INFO, statusMessage);
				}
			});
		}
	}
}
