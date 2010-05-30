package ru.greeneyes.project.pomidoro.toolkitwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import ru.greeneyes.project.pomidoro.PomodoroComponent;
import ru.greeneyes.project.pomidoro.PomodoroControlThread;
import ru.greeneyes.project.pomidoro.PomodoroController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author ivanalx
 * @date 28.04.2010 12:02:26
 */
public class PomodoroToolkitWindow extends AbstractProjectComponent {
	private static final int MAX_WORKING_TIME = 25 * 60 * 1000;
	private static final int MAX_BREAK_TIME = 5 * 60 * 1000;
	private static final String TOOL_WINDOW_ID = "Pomodoro";
	private static final String WINDOW_TITLE = "PTimer";

	private final ImageIcon pomodoroIcon = new ImageIcon(getClass().getResource("/ru/greeneyes/project/pomidoro/resources/pomodoro.png"));

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
		return "Pomodoro";
	}

	private void initToolWindow() {
		PomodoroComponent pomodoroComponent = ApplicationManager.getApplication().getComponent(PomodoroComponent.class);
		PomodoroPresenter presenter = new PomodoroPresenter(pomodoroComponent.getModel());

		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
		ToolWindow myToolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.BOTTOM);
		myToolWindow.setIcon(pomodoroIcon);
		ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
		Content content = contentFactory.createContent(presenter.getContentPane(), WINDOW_TITLE, false);
		myToolWindow.getContentManager().addContent(content);
	}

	@SuppressWarnings({"UnusedDeclaration"})
	private void initToolWindow_old() {
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);

		final PomodoroForm pomodoroForm = new PomodoroForm();

		pomodoroForm.getProgressBar().setMaximum(MAX_WORKING_TIME);
		pomodoroForm.setPomodoroAmount(0);
		pomodoroForm.getPomodorosLabel().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >=2) {
					pomodoroForm.setPomodoroAmount(0);
				}
			}
		});

		final PomodoroController pomodoroController = new PomodoroController(pomodoroForm, MAX_WORKING_TIME, MAX_BREAK_TIME);
		PomodoroControlThread t = new PomodoroControlThread(pomodoroController);

		pomodoroForm.getControlButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pomodoroController.buttonPressed();
			}
		});


		new Thread(t).start();
		JPanel myContentPanel = pomodoroForm.getRootPanel();

		ToolWindow myToolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.LEFT);
		ContentFactory contentFactory = PeerFactory.getInstance().getContentFactory();
		Content content = contentFactory.createContent(myContentPanel, WINDOW_TITLE, false);
		myToolWindow.getContentManager().addContent(content);

	}

	private void unregisterToolWindow() {
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
		toolWindowManager.unregisterToolWindow(TOOL_WINDOW_ID);
	}
}
