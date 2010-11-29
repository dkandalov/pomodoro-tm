package ru.greeneyes.project.pomidoro.modalwindow;

import javax.swing.*;

/**
 * User: dima
 * Date: Nov 29, 2010
 */
public class ModalDialogPlayground {
	public static void main(String[] args) throws InterruptedException {
		JFrame jFrame = new JFrame();
		ModalDialog modalDialog = new ModalDialog(jFrame);
		modalDialog.show();

		Thread.sleep(60 * 1000);

		modalDialog.hide();
	}
}
