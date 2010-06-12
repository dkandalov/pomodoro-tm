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
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.UI;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import ru.greeneyes.project.pomidoro.PomodoroComponent;
import ru.greeneyes.project.pomidoro.UIBundle;

import javax.swing.*;

/**
 * @author ivanalx
 * @date 28.04.2010 12:02:26
 */
public class PomodoroToolkitWindow extends AbstractProjectComponent {
	public static final String TOOL_WINDOW_ID = "Pomodoro";

	private final ImageIcon pomodoroIcon = new ImageIcon(getClass().getResource("/resources/pomodoro.png"));

	public PomodoroToolkitWindow(Project project) {
		super(project);
	}

	@Override
	public void projectOpened() {
		initToolWindow();
	}

	@Override
	public void projectClosed() {
		unregisterToolWindow();
	}

	@Override
	@NotNull
	public String getComponentName() {
		return UIBundle.message("toolkitwindow.component_name");
	}

	private void initToolWindow() {
		PomodoroComponent pomodoroComponent = ApplicationManager.getApplication().getComponent(PomodoroComponent.class);
		PomodoroPresenter presenter = new PomodoroPresenter(pomodoroComponent.getModel());

		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
		ToolWindow myToolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.BOTTOM);
		myToolWindow.setIcon(pomodoroIcon);
		ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
		Content content = contentFactory.createContent(presenter.getContentPane(), UIBundle.message("toolkitwindow.title"), false);
		myToolWindow.getContentManager().addContent(content);
	}

	private void unregisterToolWindow() {
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
		toolWindowManager.unregisterToolWindow(TOOL_WINDOW_ID);
	}
}
