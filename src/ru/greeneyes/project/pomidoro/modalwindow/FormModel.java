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

import java.util.Random;

/**
* User: dima
* Date: Nov 29, 2010
*/
class FormModel {
	private static final int OPTIONAL_CLICKS = 30;
	private static final int MIN_NUMBER_OF_CLICKS = 10;
	private int clicksToUnlock;

	public FormModel() {
		clicksToUnlock = MIN_NUMBER_OF_CLICKS + new Random().nextInt(OPTIONAL_CLICKS);
	}

	public void userClick() {
		clicksToUnlock--;
	}

	public int clicksLeft() {
		return (clicksToUnlock > 0 ? clicksToUnlock : 0);
	}

	public boolean intellijIsAllowedToBeUnlocked() {
		return clicksToUnlock <= 0;
	}
}
