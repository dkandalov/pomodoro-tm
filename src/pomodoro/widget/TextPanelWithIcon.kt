package pomodoro.widget

import com.intellij.openapi.wm.impl.status.TextPanel

import javax.swing.*
import java.awt.*

/**
 * Based on [com.intellij.openapi.wm.impl.status.TextPanel.WithIconAndArrows]
 */
internal class TextPanelWithIcon : TextPanel() {
    private var myIcon: Icon? = null

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (text == null) return

        val r = bounds
        val insets = insets
        myIcon?.paintIcon(this, g,
                insets.left - GAP - iconWidth(),
                r.height / 2 - iconWidth() / 2
        )
    }

    override fun getInsets(): Insets {
        val insets = super.getInsets()
        insets.left += iconWidth() + GAP * 2
        return insets
    }

    override fun getPreferredSize(): Dimension {
        val preferredSize = super.getPreferredSize()
        return Dimension(preferredSize.width + iconWidth(), preferredSize.height)
    }

    fun setIcon(icon: Icon?) {
        myIcon = icon
    }

    private fun iconWidth() = myIcon?.iconWidth ?: 0

    companion object {
        private val GAP = 2
    }
}
