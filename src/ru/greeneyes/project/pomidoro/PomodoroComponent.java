package ru.greeneyes.project.pomidoro;

import com.intellij.ide.DataManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
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
	private Settings settings;

	@Override
	public void initComponent() {
		settings = new Settings();
		model = new PomodoroModel(settings);

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

	public Settings getConfig() {
		return settings;
	}

	private static class UserNotifier {
		private static final String NOTIFICATION_GROUP_ID = "Pomodoro";
		
		private final AudioClip ringSound = Applet.newAudioClip(getClass().getResource("/ru/greeneyes/project/pomidoro/resources/ring.wav"));
		public PomodoroModel.PomodoroState lastState;

		public UserNotifier(final Settings settings, final PomodoroModel model) {
			model.addUpdateListener(this, new Runnable() {
				@Override
				public void run() {
					switch (model.getState()) {
						case STOP:
							if (lastState == BREAK && !model.wasManuallyStopped()) {
								if (settings.isRingEnabled()) ringSound.play();
							}
							break;
						case BREAK:
							if (lastState != BREAK) {
								if (settings.isRingEnabled()) ringSound.play();
								if (settings.isPopupEnabled()) showPopupNotification();
							}
							break;
					}
					lastState = model.getState();
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
							NOTIFICATION_GROUP_ID, "Pomodoro is finished", "Please have a break",
							NotificationType.INFORMATION
					),
					project
			);
		}
	}
}
