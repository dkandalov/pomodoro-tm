package pomodoro.modalwindow

import javax.swing.JFrame

fun main(args: Array<String>) {
    ModalDialog(JFrame()).apply {
        show()
        Thread.sleep(60 * 1000L)
        hide()
    }
}
