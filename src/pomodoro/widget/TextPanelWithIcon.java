package pomodoro.widget;

import com.intellij.openapi.wm.impl.status.TextPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Based on {@link com.intellij.openapi.wm.impl.status.TextPanel.WithIconAndArrows}
 */
class TextPanelWithIcon extends TextPanel {
	private final static int GAP = 2;
	@Nullable
	private Icon myIcon;

	@Override
	protected void paintComponent(@NotNull final Graphics g) {
		super.paintComponent(g);
		if (getText() == null) return;

		Rectangle r = getBounds();
		Insets insets = getInsets();
		if (myIcon != null) {
			myIcon.paintIcon(this, g,
					insets.left - GAP - myIcon.getIconWidth(),
					r.height / 2 - myIcon.getIconHeight() / 2
			);
		}
	}

	@NotNull
	@Override
	public Insets getInsets() {
		Insets insets = super.getInsets();
		if (myIcon != null) {
			insets.left += myIcon.getIconWidth() + GAP * 2;
		}
		return insets;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = super.getPreferredSize();
		int deltaWidth = 0;
		if (myIcon != null) {
			deltaWidth += myIcon.getIconWidth();
		}
		return new Dimension(preferredSize.width + deltaWidth, preferredSize.height);
	}

	public void setIcon(@Nullable Icon icon) {
		myIcon = icon;
	}
}
