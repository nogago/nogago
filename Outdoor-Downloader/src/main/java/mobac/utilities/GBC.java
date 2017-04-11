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
// License: GPL. Copyright 2007 by Immanuel Scholz and others
package mobac.utilities;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.Box;

/**
 * A wrapper for GridBagConstraints which has sane default static creators and
 * member functions to chain calling.
 * 
 * @author imi
 */
public class GBC extends GridBagConstraints {

	private static final long serialVersionUID = 1L;

	/**
	 * Use public static creator functions to create an GBC.
	 */
	private GBC() {
	}

	/**
	 * Create a standard constraint (which is not the last).
	 * 
	 * @return A standard constraint with no filling.
	 */
	public static GBC std() {
		GBC c = new GBC();
		c.anchor = WEST;
		return c;
	}

	/**
	 * Create the constraint for the last elements on a line.
	 * 
	 * @return A constraint which indicates the last item on a line.
	 */
	public static GBC eol() {
		GBC c = std();
		c.gridwidth = REMAINDER;
		return c;
	}

	/**
	 * Create the constraint for the last elements on a line and on a paragraph.
	 * This is merely a shortcut for eol().insets(0,0,0,10)
	 * 
	 * @return A constraint which indicates the last item on a line.
	 */
	public static GBC eop() {
		return eol().insets(0, 0, 0, 10);
	}

	/**
	 * Try to fill both, horizontal and vertical
	 * 
	 * @return This constraint for chaining.
	 */
	public GBC fill() {
		return fill(BOTH);
	}

	public GBC toggleEol() {
		if (gridwidth == GBC.REMAINDER)
			gridwidth = 1;
		else
			gridwidth = GBC.REMAINDER;
		return this;
	}

	public GBC gridwidth(int value) {
		gridwidth = value;
		return this;
	}

	public GBC gridheight(int value) {
		gridheight = value;
		return this;
	}

	public GBC gridx(int value) {
		gridx = value;
		return this;
	}

	public GBC gridxy(int x, int y) {
		gridx = x;
		gridy = y;
		return this;
	}

	public GBC gridy(int value) {
		gridy = value;
		return this;
	}

	/**
	 * Set fill to the given value
	 * 
	 * @param value
	 *            The filling value, either NONE, HORIZONTAL, VERTICAL or BOTH
	 * @return This constraint for chaining.
	 */
	public GBC fill(int value) {
		fill = value;
		if (value == HORIZONTAL || value == BOTH)
			weightx = 1.0;
		if (value == VERTICAL || value == BOTH)
			weighty = 1.0;
		return this;
	}

	public GBC fillH() {
		return fill(GBC.HORIZONTAL);
	}

	/**
	 * Set the anchor of this GBC to a.
	 * 
	 * @param a
	 *            The new anchor, e.g. GBC.CENTER or GBC.EAST.
	 * @return This constraint for chaining.
	 */
	public GBC anchor(int a) {
		anchor = a;
		return this;
	}

	/**
	 * Adds insets to this GBC.
	 * 
	 * @param left
	 *            The left space of the insets
	 * @param top
	 *            The top space of the insets
	 * @param right
	 *            The right space of the insets
	 * @param bottom
	 *            The bottom space of the insets
	 * @return This constraint for chaining.
	 */
	public GBC insets(int left, int top, int right, int bottom) {
		insets = new Insets(top, left, bottom, right);
		return this;
	}

	/**
	 * This is a helper to easily create a glue with a minimum default value.
	 * 
	 * @param x
	 *            If higher than 0, this will be a horizontal glue with x as
	 *            minimum horizontal strut.
	 * @param y
	 *            If higher than 0, this will be a vertical glue with y as
	 *            minimum vertical strut.
	 */
	public static Component glue(int x, int y) {
		short maxx = x > 0 ? Short.MAX_VALUE : 0;
		short maxy = y > 0 ? Short.MAX_VALUE : 0;
		return new Box.Filler(new Dimension(x, y), new Dimension(x, y), new Dimension(maxx, maxy));
	}
}
