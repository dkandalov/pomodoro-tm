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
package pomodoro;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import pomodoro.model.PomodoroModel;
import pomodoro.toolkitwindow.PomodoroPresenter;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PomodoroWidget implements CustomStatusBarWidget, StatusBarWidget.Multiframe {
	private final ImageIcon pomodoroIcon = new ImageIcon(getClass().getResource("/resources/pomodoro.png"));
	private final ImageIcon pomodoroStoppedIcon = new ImageIcon(getClass().getResource("/resources/pomodoroStopped.png"));
	private final ImageIcon pomodoroBreakIcon = new ImageIcon(getClass().getResource("/resources/pomodoroBreak.png"));
	private StatusBar statusBar;

	@Override
	public void install(@NotNull StatusBar statusBar) {
		this.statusBar = statusBar;
	}

	@Override
	public JComponent getComponent() {
		final JLabel label = new JBLabel();
		label.setFont(JBUI.Fonts.label(11));

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
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				model.switchToNextState();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				String tooltipText = tooltipText(model);
				statusBar.setInfo(tooltipText);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				statusBar.setInfo("");
			}
		});

		label.setBorder(StatusBarWidget.WidgetBorder.WIDE);
		return label;
	}

	@Override
	public StatusBarWidget copy() {
		return new PomodoroWidget();
	}

	private String tooltipText(PomodoroModel model) {
		String nextAction = "";
		switch (model.getState()) {
			case STOP:
				nextAction = UIBundle.message("statuspanel.start");
				break;
			case RUN:
				nextAction = UIBundle.message("statuspanel.stop");
				break;
			case BREAK:
				nextAction = UIBundle.message("statuspanel.stop_break");
				break;
		}
		return UIBundle.message("statuspanel.tooltip", nextAction, model.getPomodorosAmount());
	}

	private void updateLabel(PomodoroModel model, JLabel label) {
		int timeLeft = model.getProgressMax() - model.getProgress();
		label.setText(PomodoroPresenter.formatTime(timeLeft));
		label.setIcon(getIcon(model));
	}

	@NotNull
	private ImageIcon getIcon(PomodoroModel model) {
		switch (model.getState()) {
			case STOP: return pomodoroStoppedIcon;
			case RUN: return pomodoroIcon;
			case BREAK: return pomodoroBreakIcon;
			default: throw new IllegalStateException();
		}
	}

	@Override
	public WidgetPresentation getPresentation(@NotNull PlatformType type) {
		return null;
	}

	@Override
	public void dispose() {
	}

	@NotNull
	@Override
	public String ID() {
		return "Pomodoro";
	}
}
