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
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pomodoro.model.ChangeListener;
import pomodoro.model.PomodoroModel;
import pomodoro.model.Settings;
import pomodoro.toolkitwindow.PomodoroPresenter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static pomodoro.PomodoroComponent.getSettings;

public class PomodoroWidget implements CustomStatusBarWidget, StatusBarWidget.Multiframe, ChangeListener {
	private final ImageIcon pomodoroIcon = new ImageIcon(getClass().getResource("/resources/pomodoro.png"));
	private final ImageIcon pomodoroStoppedIcon = new ImageIcon(getClass().getResource("/resources/pomodoroStopped.png"));
	private final ImageIcon pomodoroBreakIcon = new ImageIcon(getClass().getResource("/resources/pomodoroBreak.png"));
	private final ImageIcon pomodoroDarculaIcon = new ImageIcon(getClass().getResource("/resources/pomodoro-inverted.png"));
	private final ImageIcon pomodoroStoppedDarculaIcon = new ImageIcon(getClass().getResource("/resources/pomodoroStopped-inverted.png"));
	private final ImageIcon pomodoroBreakDarculaIcon = new ImageIcon(getClass().getResource("/resources/pomodoroBreak-inverted.png"));
	private final TextPanelWithIcon panelWithIcon;
	private StatusBar statusBar;
	private final PomodoroModel model;


	public PomodoroWidget() {
		panelWithIcon = new TextPanelWithIcon();
		final Settings settings = getSettings();

		PomodoroComponent pomodoroComponent = ApplicationManager.getApplication().getComponent(PomodoroComponent.class);
		assert pomodoroComponent != null;
		model = pomodoroComponent.getModel();
		updateWidgetPanel(model, panelWithIcon, settings.isShowTimeInToolbarWidget());

		model.addUpdateListener(panelWithIcon, new Runnable() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateWidgetPanel(model, panelWithIcon, settings.isShowTimeInToolbarWidget());
					}
				});
			}
		});
		panelWithIcon.addMouseListener(new MouseAdapter() {
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
	}

	@Override
	public void install(@NotNull StatusBar statusBar) {
		this.statusBar = statusBar;
	}

	@Override
	public JComponent getComponent() {
		return panelWithIcon;
	}

	@Override
	public StatusBarWidget copy() {
		return new PomodoroWidget();
	}

	private String tooltipText(PomodoroModel model) {
		String nextAction = nextActionName(model);
		int pomodorosAmount = model.getPomodorosAmount();
		return UIBundle.message("statuspanel.tooltip", nextAction, pomodorosAmount);
	}

	@NotNull
	private static String nextActionName(PomodoroModel model) {
		switch (model.getState()) {
			case STOP: return UIBundle.message("statuspanel.start");
			case RUN: return UIBundle.message("statuspanel.stop");
			case BREAK: return UIBundle.message("statuspanel.stop_break");
			default: return "";
		}
	}

	private void updateWidgetPanel(PomodoroModel model, TextPanelWithIcon panelWithIcon, boolean showTimeInToolbarWidget) {
		if (showTimeInToolbarWidget) {
			int timeLeft = model.getProgressMax() - model.getProgress();
			panelWithIcon.setText(PomodoroPresenter.formatTime(timeLeft));
		} else {
			panelWithIcon.setText("");
		}
		panelWithIcon.setIcon(getIcon(model));
		panelWithIcon.repaint();
	}

	@NotNull
	private ImageIcon getIcon(PomodoroModel model) {
		boolean underDarcula = UIUtil.isUnderDarcula();
		switch (model.getState()) {
			case STOP: return underDarcula ? pomodoroStoppedDarculaIcon : pomodoroStoppedIcon;
			case RUN: return underDarcula ? pomodoroDarculaIcon : pomodoroIcon;
			case BREAK: return underDarcula ? pomodoroBreakDarculaIcon : pomodoroBreakIcon;
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

	@Override
	public void onChange(Settings settings) {
		updateWidgetPanel(model, panelWithIcon, settings.isShowTimeInToolbarWidget());
	}


	/**
	 * Based on {@link com.intellij.openapi.wm.impl.status.TextPanel.WithIconAndArrows}
	 */
	private static class TextPanelWithIcon extends TextPanel {
		private final static int GAP = 2;
		@Nullable
		private Icon myIcon;

		@Override
		protected void paintComponent(@NotNull final Graphics g) {
			super.paintComponent(g);
			if (getText() == null) return;

			Rectangle r = getBounds();
			Insets insets = getInsets();
			if (myIcon != null) {
				myIcon.paintIcon(this, g,
						insets.left - GAP - myIcon.getIconWidth(),
						r.height / 2 - myIcon.getIconHeight() / 2
				);
			}
		}

		@NotNull
		@Override
		public Insets getInsets() {
			Insets insets = super.getInsets();
			if (myIcon != null) {
				insets.left += myIcon.getIconWidth() + GAP * 2;
			}
			return insets;
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension preferredSize = super.getPreferredSize();
			int deltaWidth = 0;
			if (myIcon != null) {
				deltaWidth += myIcon.getIconWidth();
			}
			return new Dimension(preferredSize.width + deltaWidth, preferredSize.height);
		}

		public void setIcon(@Nullable Icon icon) {
			myIcon = icon;
		}
	}

}
