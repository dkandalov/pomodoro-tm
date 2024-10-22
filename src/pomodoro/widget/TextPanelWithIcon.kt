package pomodoro.widget

import com.intellij.openapi.wm.impl.status.TextPanel
import java.awt.Graphics
import java.awt.Insets
import javax.swing.Icon

/**
 * Based on [com.intellij.openapi.wm.impl.status.TextPanel.WithIconAndArrows]
 */
internal class TextPanelWithIcon: TextPanel() {
    private val gap = 2

    var icon: Icon? = null

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (text == null) return

        icon?.paintIcon(
            this, g,
            insets.left - gap - iconWidth(),
            bounds.height / 2 - iconWidth() / 2
        )
    }

    override fun getInsets(): Insets {
        val insets = super.getInsets()
        insets.left += iconWidth() + gap * 2
        return insets
    }

    private fun iconWidth() = icon?.iconWidth ?: 0
}
