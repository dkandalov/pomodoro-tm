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
package pomodoro.modalwindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.VK_ESCAPE;

/**
 * User: dima
 * Date: Nov 29, 2010
 */
public class ModalDialog {
	private final JDialog jDialog;

	public ModalDialog(final Window window) {
		final FormModel model = new FormModel();

		if (window instanceof Dialog) {
			jDialog = new JDialog((Dialog) window);
		} else {
			jDialog = new JDialog((Frame) window);
		}
		ModalForm form = new ModalForm(model);
		jDialog.setModal(true);
		jDialog.setUndecorated(true);
		jDialog.setContentPane(form.rootPanel);
		jDialog.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				boolean pressedEscape = (keyEvent.getKeyCode() == VK_ESCAPE) && (keyEvent.getModifiers() == 0);
				if (pressedEscape && model.intellijIsAllowedToBeUnlocked()) {
					jDialog.dispose();
				}
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
