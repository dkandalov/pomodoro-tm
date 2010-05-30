package ru.greeneyes.project.pomidoro;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;

/**
 * User: dima
 * Date: May 30, 2010
 */
public class PomodoroAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent event) {
		PomodoroComponent pomodoroComponent = ApplicationManager.getApplication().getComponent(PomodoroComponent.class);
		pomodoroComponent.getModel().switchToNextState();
	}
}
