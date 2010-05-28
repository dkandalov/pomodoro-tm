package ru.greeneyes.project.pomidoro;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

/**
 * @author ivanalx
 * @date 28.04.2010 18:37:08
 */
public class PomodoroControlThread implements Runnable {
	private PomodoroController controller;

	public PomodoroControlThread(PomodoroController controller) {
		this.controller = controller;
	}

	public void run() {
		while (!Thread.interrupted()) {
			controller.update();
			sleep100ms();
		}
	}

	private void sleep100ms() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
