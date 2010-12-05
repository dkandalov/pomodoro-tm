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
