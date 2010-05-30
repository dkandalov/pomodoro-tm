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
		model.addUpdateListener(label, new Runnable() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						label.setText(PomodoroPresenter.formatTime(model.getProgress()));
					}
				});
			}
		});
		return label;
	}
}
