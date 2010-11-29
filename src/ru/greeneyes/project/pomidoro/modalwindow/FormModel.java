package ru.greeneyes.project.pomidoro.modalwindow;

import java.util.Random;

/**
* User: dima
* Date: Nov 29, 2010
*/
class FormModel {
	private static final int OPTIONAL_CLICKS = 1;
	private static final int MIN_NUMBER_OF_CLICKS = 1;
	private int clicksToUnlock;

	public FormModel() {
		clicksToUnlock = MIN_NUMBER_OF_CLICKS + new Random().nextInt(OPTIONAL_CLICKS);
	}

	public boolean dialogIsAllowedToBeClosed() {
		return clicksToUnlock <= 0;
	}

	public void userClick() {
		clicksToUnlock--;
	}

	public int clicksLeft() {
		return (clicksToUnlock > 0 ? clicksToUnlock : 0);
	}
}
