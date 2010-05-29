package ru.greeneyes.project.pomidoro;

import javax.swing.*;

/**
 * User: dima
 * Date: May 29, 2010
 */
public class Pomodoro_Playground {
	public static void main(String[] args) {
		PomodoroModel model = new PomodoroModel(new MyConfig());
		PomodoroPresenter presenter = new PomodoroPresenter(model);

		JFrame jFrame = new JFrame();
		jFrame.setContentPane(presenter.getContentPane());
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.pack();
		jFrame.setVisible(true);
	}

	private static class MyConfig extends Config {
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
