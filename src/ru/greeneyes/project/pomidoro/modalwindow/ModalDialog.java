package ru.greeneyes.project.pomidoro.modalwindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * User: dima
 * Date: Nov 29, 2010
 */
public class ModalDialog {
	private final JDialog jDialog;

	public ModalDialog(Window window) {
		final FormModel model = new FormModel();

		if (window instanceof Dialog) {
			jDialog = new JDialog((Dialog) window);
		} else {
			jDialog = new JDialog((Frame) window);
		}
		ModalForm form = new ModalForm(model, jDialog);
		jDialog.setModal(true);
		jDialog.setUndecorated(true);
		jDialog.setContentPane(form.rootPanel);
		jDialog.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent keyEvent) {
			}
		});

		jDialog.pack();
		jDialog.setLocationRelativeTo(window);
	}

	public void show() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				jDialog.setVisible(true);
			}
		});
	}

	public void hide() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				jDialog.dispose();
			}
		});
	}

}
