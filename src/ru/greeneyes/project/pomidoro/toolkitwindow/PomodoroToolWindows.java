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
package ru.greeneyes.project.pomidoro.toolkitwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import ru.greeneyes.project.pomidoro.PomodoroComponent;
import ru.greeneyes.project.pomidoro.UIBundle;
import ru.greeneyes.project.pomidoro.model.ChangeListener;
import ru.greeneyes.project.pomidoro.model.Settings;

import javax.swing.*;

/**
 * @author ivanalx
 * @date 28.04.2010 12:02:26
 */
public class PomodoroToolWindows implements ChangeListener {
	public static final String TOOL_WINDOW_ID = "Pomodoro";

	private static final ImageIcon pomodoroIcon = new ImageIcon(PomodoroToolWindows.class.getResource("/resources/pomodoro-icon.png"));

	public PomodoroToolWindows() {
		ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerListener() {
			@Override
			public void projectOpened(Project project) {
				if (PomodoroComponent.getSettings().isShowToolWindow()) {
					registerWindowFor(project);
				}
			}

			@Override
			public void projectClosed(Project project) {
				// unregister window in any case
				unregisterWindowFrom(project);
			}

			@Override
			public boolean canCloseProject(Project project) {
				return true;
			}

			@Override
			public void projectClosing(Project project) {
			}
		});
	}

	@Override
	public void onChange(Settings settings) {
		Project[] projects = ProjectManager.getInstance().getOpenProjects();
		for (Project project : projects) {
			if (settings.isShowToolWindow()) {
				registerWindowFor(project);
			} else {
				unregisterWindowFrom(project);
			}
		}
	}

	public void registerWindowFor(Project project) {
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
		if (toolWindowManager.getToolWindow(TOOL_WINDOW_ID) != null) {
			return;
		}

		PomodoroComponent pomodoroComponent = ApplicationManager.getApplication().getComponent(PomodoroComponent.class);
		PomodoroPresenter presenter = new PomodoroPresenter(pomodoroComponent.getModel());

		ToolWindow myToolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.BOTTOM);
		myToolWindow.setIcon(pomodoroIcon);
		ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
		Content content = contentFactory.createContent(presenter.getContentPane(), UIBundle.message("toolwindow.title"), false);
		myToolWindow.getContentManager().addContent(content);
	}

	public void unregisterWindowFrom(Project project) {
		final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
		if (toolWindowManager.getToolWindow(TOOL_WINDOW_ID) != null) {
			toolWindowManager.unregisterToolWindow(TOOL_WINDOW_ID);
		}
	}
}
