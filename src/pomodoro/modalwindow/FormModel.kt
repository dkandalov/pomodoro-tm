package pomodoro.modalwindow

import java.util.*

internal class FormModel {
    private val optionalClicks = 30
    private val minNumberOfClicks = 10
    private var clicksToUnlock = minNumberOfClicks + Random().nextInt(optionalClicks)

    fun userClick() {
        clicksToUnlock--
    }

    fun clicksLeft() = if (clicksToUnlock > 0) clicksToUnlock else 0

    fun intellijIsAllowedToBeUnlocked() = clicksToUnlock <= 0
}
