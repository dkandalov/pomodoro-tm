package ru.greeneyes.project.pomidoro;

import ru.greeneyes.project.pomidoro.model.Settings;
import ru.greeneyes.project.pomidoro.model.ControlThread;
import ru.greeneyes.project.pomidoro.model.PomodoroModel;
import ru.greeneyes.project.pomidoro.toolkitwindow.PomodoroPresenter;

import javax.swing.*;

/**
 * User: dima
 * Date: May 29, 2010
 */
public class Pomodoro_Playground {
	public static void main(String[] args) {
		MySettings config = new MySettings();
		PomodoroModel model = new PomodoroModel(config);
		PomodoroPresenter presenter = new PomodoroPresenter(model);
		new ControlThread(model).start();

		JFrame jFrame = new JFrame();
		jFrame.setContentPane(presenter.getContentPane());
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.pack();
		jFrame.setVisible(true);
	}

	private static class MySettings extends Settings {
		@Override
		public long getPomodoroLength() {
			return 5 * 1000;
		}

		@Override
		public long getBreakLength() {
			return 5 * 1000;
		}
	}
}
