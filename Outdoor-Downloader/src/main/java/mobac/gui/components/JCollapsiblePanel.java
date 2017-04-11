/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import mobac.utilities.GBC;
import mobac.utilities.Utilities;


/**
 * Bases upon "TitleContainer" from project "swivel"
 * http://code.google.com/p/swivel/ (LGPL license)
 * 
 * A {@code TitleContainer} is a simple container that provides an easily
 * visible title.
 * 
 */
public class JCollapsiblePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	protected static final int DEFAULT_TITLE_PADDING = 3;
	protected static final Color DEFAULT_TITLE_BACKGROUND_COLOR = Color.LIGHT_GRAY;
	protected static final Color DEFAULT_TITLE_COLOR = Color.BLACK;

	private static ImageIcon arrowClosed;
	private static ImageIcon arrowOpen;

	static {
		try {
			arrowClosed = Utilities.loadResourceImageIcon("arrow_closed.png");
			arrowOpen = Utilities.loadResourceImageIcon("arrow_open.png");
		} catch (Exception e) {
			arrowClosed = new ImageIcon();
			arrowOpen = new ImageIcon();
		}
	}

	// title
	protected final JLabel titleIcon;
	protected final JLabel titleLabel;
	protected final JPanel titlePanel;

	// main component
	protected Container contentContainer;

	// collapsing
	protected final CollapsingMouseListener collapsingMouseListener;
	private boolean isCollapsed;

	/**
	 * Constructs a {@code TitleContainer} that wraps the specified container.
	 * 
	 * @param container
	 *            the main container
	 */
	public JCollapsiblePanel(Container container) {
		this(container, "");
	}

	public JCollapsiblePanel(String title) {
		this(new JPanel(), title);
	}

	public JCollapsiblePanel(String title, LayoutManager layout) {
		this(new JPanel(layout), title);
		setName(title);
	}

	/**
	 * Constructs a {@code TitleContainer} that wraps the specified component
	 * and has the specified title.
	 * 
	 * @param container
	 *            the main container
	 * @param title
	 *            the title
	 */
	public JCollapsiblePanel(Container container, String title) {
		super();
		setName(title);
		titleIcon = new JLabel(arrowOpen);
		titleLabel = new JLabel(title);
		titlePanel = new JPanel(new GridBagLayout());
		// mainComponentPanel = new JPanel(new GridBagLayout());
		collapsingMouseListener = new CollapsingMouseListener();

		titleIcon.setMinimumSize(new Dimension(40, 40));
		titleIcon.setPreferredSize(titleIcon.getPreferredSize());

		// set collapse behavior
		titlePanel.addMouseListener(collapsingMouseListener);

		// look and feel
		setTitleBackgroundColor(DEFAULT_TITLE_BACKGROUND_COLOR);
		setTitleColor(DEFAULT_TITLE_COLOR);
		setTitleBarPadding(DEFAULT_TITLE_PADDING);

		// layout
		titlePanel.add(titleIcon, GBC.std());
		titlePanel.add(titleLabel, GBC.std().insets(5, 0, 1, 0));
		titlePanel.add(Box.createHorizontalGlue(), GBC.eol().fill());
		setLayout(new BorderLayout());
		add(titlePanel, BorderLayout.NORTH);
		add(container, BorderLayout.CENTER);
		setContentContainer(container);
		setBorder(BorderFactory.createEtchedBorder());

	}

	/**
	 * This method provides a programmatic way to collapse the container.
	 * 
	 * @param collapsed
	 *            whether the container should be collapsed or shown
	 */
	public void setCollapsed(boolean collapsed) {
		if (isCollapsed == collapsed) {
			return;
		}

		if (collapsed) {
			titleIcon.setIcon(arrowClosed);

			// We have to make sure that the panel width does not shrink because
			// of the hidden content of contentContainer
			Dimension dcont = contentContainer.getLayout().preferredLayoutSize(contentContainer);
			Dimension pref = titlePanel.getPreferredSize();
			titlePanel.setPreferredSize(new Dimension(dcont.width, pref.height));
		} else {
			titleIcon.setIcon(arrowOpen);
		}
		contentContainer.setVisible(!collapsed);

		isCollapsed = collapsed;
		revalidate();
	}

	/**
	 * Gets whether the container is collapsed or not.
	 * 
	 * @return whether the container is collapsed or not
	 */
	public boolean isCollapsed() {
		return isCollapsed;
	}

	/**
	 * Sets the title of this container.
	 * 
	 * @param title
	 *            the title
	 */
	public void setTitle(String title) {
		if (title != null) {
			titleLabel.setText(title);
		} else {
			titleLabel.setText("");
		}
	}

	/**
	 * Gets the this container's title.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return titleLabel.getText();
	}

	/**
	 * Sets the main content container for this container.
	 * 
	 * @param container
	 *            the main content container
	 */
	public void setContentContainer(Container container) {
		// don't need to do anything if the main component hasn't changed
		if (container == this.contentContainer) {
			return;
		}

		// remove the main component
		if (this.contentContainer != null) {
			remove(this.contentContainer);
		}

		// replace the main component
		this.contentContainer = container;
		add(container, BorderLayout.CENTER);

		// repaint main component
		revalidate();
	}

	/**
	 * Gets the main content container for this container.
	 * 
	 * @return the main container
	 */
	public Container getContentContainer() {
		return contentContainer;
	}

	public void addContent(Component comp, Object constraints) {
		contentContainer.add(comp, constraints);
	}

	/**
	 * Sets the title font.
	 * 
	 * @param font
	 *            the title font
	 */
	public void setTitleFont(Font font) {
		super.setFont(font);
		if (titleLabel != null) {
			titleLabel.setFont(font);
		}
	}

	/**
	 * Sets the background color of the title bar.
	 * 
	 * @param color
	 *            the color
	 */
	public void setTitleBackgroundColor(Color color) {
		this.titlePanel.setBackground(color);
	}

	/**
	 * Sets the title text color.
	 * 
	 * @param color
	 *            the color
	 */
	public void setTitleColor(Color color) {
		this.titleLabel.setForeground(color);
	}

	/**
	 * Sets the title bar padding in pixels.
	 * 
	 * @param padding
	 *            the title bar padding in pixels
	 */
	public void setTitleBarPadding(int padding) {
		Border border = BorderFactory.createEmptyBorder(padding, padding, padding, padding);
		this.titlePanel.setBorder(border);
	}

	/**
	 * Sets the visibility of the title bar. If the title bar is invisible, the
	 * user will not be able to collapse or decollapse the container.
	 * 
	 * @param visible
	 *            visibility of the title bar
	 */
	public void setTitleBarVisible(boolean visible) {
		this.titlePanel.setVisible(visible);
	}

	/**
	 * Gets the visibility of the title bar.
	 * 
	 * @return true if the title bar is visible, false otherwise
	 */
	public boolean isTitleBarVisible() {
		return this.titlePanel.isVisible();
	}

	//--------------------------------------------------------------------------

	/**
	 * A {@code MouseListener} that changes the cursor when moved over the title
	 * bar to indicate that it is clickable. Clicking the title bar collapses
	 * the container.
	 */
	private class CollapsingMouseListener extends MouseAdapter {
		private final Cursor CLICK_ME_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

		/**
		 * Collapses or shows the titled component.
		 * 
		 * {@inheritDoc}
		 */
		public void mousePressed(MouseEvent e) {
			setCollapsed(!JCollapsiblePanel.this.isCollapsed);
		}

		/**
		 * Changes the cursor to indicate clickability.
		 * 
		 * {@inheritDoc}
		 */
		public void mouseEntered(MouseEvent e) {
			titlePanel.setCursor(CLICK_ME_CURSOR);
		}

		/**
		 * Changes the cursor back to the default.
		 * 
		 * {@inheritDoc}
		 */
		public void mouseExited(MouseEvent e) {
			titlePanel.setCursor(Cursor.getDefaultCursor());
		}
	}

}
