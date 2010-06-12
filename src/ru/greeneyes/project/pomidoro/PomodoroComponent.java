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
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import ru.greeneyes.project.pomidoro.model.PomodoroModelState;
import ru.greeneyes.project.pomidoro.model.Settings;
import ru.greeneyes.project.pomidoro.model.ControlThread;
import ru.greeneyes.project.pomidoro.model.PomodoroModel;

import java.applet.Applet;
import java.applet.AudioClip;

import static ru.greeneyes.project.pomidoro.model.PomodoroModel.PomodoroState.BREAK;

/**
 * User: dima
 * Date: May 30, 2010
 */
public class PomodoroComponent implements ApplicationComponent {
	private ControlThread controlThread;
	private PomodoroModel model;

	@Override
	public void initComponent() {
		Settings settings = ServiceManager.getService(Settings.class);
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
		return "Pomodoro";
	}

	public PomodoroModel getModel() {
		return model;
	}

	private static class UserNotifier {
		private static final String NOTIFICATION_GROUP_ID = "Pomodoro";
		
		private final AudioClip ringSound = Applet.newAudioClip(getClass().getResource("/resources/ring.wav"));

		public UserNotifier(final Settings settings, final PomodoroModel model) {
			model.addUpdateListener(this, new Runnable() {
				@Override
				public void run() {
					switch (model.getState()) {
						case STOP:
							if (model.getLastState() == BREAK && !model.wasManuallyStopped()) {
								if (settings.isRingEnabled()) ringSound.play();
							}
							break;
						case BREAK:
							if (model.getLastState() != BREAK) {
								if (settings.isRingEnabled()) ringSound.play();
								if (settings.isPopupEnabled()) showPopupNotification();
							}
							break;
					}
				}
			});
		}

		private void showPopupNotification() {
			// we get project as a workaround for this issue http://youtrack.jetbrains.net/issue/IDEA-53248
			// (posting notification without specifying a project results in showing two balloons)
			DataContext dataContext = DataManager.getInstance().getDataContext();
			Project project = (Project) dataContext.getData(PlatformDataKeys.PROJECT.getName());

			Notifications.Bus.notify(
					new Notification(
							NOTIFICATION_GROUP_ID,
							UIBundle.message("notification.title"),
							UIBundle.message("notification.text"),
							NotificationType.INFORMATION
					),
					project
			);
		}
	}
}
