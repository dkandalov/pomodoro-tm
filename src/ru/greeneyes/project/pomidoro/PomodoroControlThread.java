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
package ru.greeneyes.project.pomidoro;

/**
 * @author ivanalx
 * @date 28.04.2010 18:37:08
 */
@Deprecated
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
