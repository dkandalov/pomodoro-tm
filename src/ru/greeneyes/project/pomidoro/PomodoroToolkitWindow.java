package ru.greeneyes.project.pomidoro;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

/**
 * @author ivanalx
 * @date 28.04.2010 12:02:26
 */
public class PomodoroToolkitWindow implements ProjectComponent {
	private final Project project;

	private ToolWindow myToolWindow;
	private JPanel myContentPanel;

	public static final int MAX_WORKING_TIME = 25 * 60 * 1000;
	public static final int MAX_BREAK_TIME = 5 * 60 * 1000;


	public static final String TOOL_WINDOW_ID = "Pomodoro";


	public PomodoroToolkitWindow(Project project) {
		this.project = project;
	}

	public void projectOpened() {
		initToolWindow();
	}

	public void projectClosed() {
		unregisterToolWindow();
	}

	@NotNull
	public String getComponentName() {
		return "Pomodoro";
	}

	public void initComponent() {

	}

	public void disposeComponent() {

	}


	private void initToolWindow() {
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);

		final PomodoroForm pomodoroForm = createContantPane();

		pomodoroForm.getProgressBar1().setMaximum(MAX_WORKING_TIME);
		pomodoroForm.setPomodoroAmount(0);
		pomodoroForm.getPomodorosLabel().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >=2) {
					pomodoroForm.setPomodoroAmount(0);
				}
			}
		});

		final PomodoroController pomodoroController = new PomodoroController(project, pomodoroForm, MAX_WORKING_TIME, MAX_BREAK_TIME);
		PomodoroControlThread t = new PomodoroControlThread(pomodoroController);

		pomodoroForm.getControllButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pomodoroController.buttonPressed();
			}
		});


		new Thread(t).start();
		myContentPanel = pomodoroForm.getRootPanel();

		myToolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.LEFT);
		ContentFactory contentFactory = PeerFactory.getInstance().getContentFactory();
		Content content = contentFactory.createContent(myContentPanel, "PTimer", false);
		myToolWindow.getContentManager().addContent(content);

	}

	private static PomodoroForm createContantPane() {
		PomodoroForm pomidoroForm = new PomodoroForm();
		return pomidoroForm;
	}

	private void unregisterToolWindow() {
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
		toolWindowManager.unregisterToolWindow(TOOL_WINDOW_ID);
	}
}
