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
package mobac.program.model;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

public class PaperSize {

	public static enum Format {
		A0(PageSize.A0), A1(PageSize.A1), A2(PageSize.A2), A3(PageSize.A3), A4(PageSize.A4), A5(PageSize.A5), A6(
				PageSize.A6), A7(PageSize.A7), A8(PageSize.A8), A9(PageSize.A9), A10(PageSize.A10), ARCH_A(
				PageSize.ARCH_A), ARCH_B(PageSize.ARCH_B), ARCH_C(PageSize.ARCH_C), ARCH_D(PageSize.ARCH_D), ARCH_E(
				PageSize.ARCH_E), B0(PageSize.B0), B1(PageSize.B1), B2(PageSize.B2), B3(PageSize.B3), B4(PageSize.B4), B5(
				PageSize.B5), B6(PageSize.B6), B7(PageSize.B7), B8(PageSize.B8), B9(PageSize.B9), B10(PageSize.B10),
		// CROWN_OCTAVO(PageSize.CROWN_OCTAVO),
		// CROWN_QUARTO(PageSize.CROWN_QUARTO),
		// DEMY_OCTAVO(PageSize.DEMY_OCTAVO),
		// DEMY_QUARTO(PageSize.DEMY_QUARTO),
		// EXECUTIVE(PageSize.EXECUTIVE),
		// FLSA(PageSize.FLSA),
		// FLSE(PageSize.FLSE),
		// HALFLETTER(PageSize.HALFLETTER),
		// ID_1(PageSize.ID_1),
		// ID_2(PageSize.ID_2),
		// ID_3(PageSize.ID_3),
		// LARGE_CROWN_OCTAVO(PageSize.LARGE_CROWN_OCTAVO),
		// LARGE_CROWN_QUARTO(PageSize.LARGE_CROWN_QUARTO),
		// LEDGER(PageSize.LEDGER),
		// LEGAL(PageSize.LEGAL),
		// LETTER(PageSize.LETTER),
		// NOTE(PageSize.NOTE),
		// PENGUIN_LARGE_PAPERBACK(PageSize.PENGUIN_LARGE_PAPERBACK),
		// PENGUIN_SMALL_PAPERBACK(PageSize.PENGUIN_SMALL_PAPERBACK),
		// POSTCARD(PageSize.POSTCARD),
		// ROYAL_OCTAVO(PageSize.ROYAL_OCTAVO),
		// ROYAL_QUARTO(PageSize.ROYAL_QUARTO),
		// SMALL_PAPERBACK(PageSize.SMALL_PAPERBACK),
		// TABLOID(PageSize.TABLOID)
		;

		public final float width, height;

		private Format(final Rectangle rectangle) {
			width = rectangle.getWidth();
			height = rectangle.getHeight();
		}
	}

	public final double width, height;
	public final boolean landscape;
	public final Format format;

	public PaperSize(Format format, boolean landscape) {
		if (landscape) {
			width = format.height;
			height = format.width;
		} else {
			width = format.width;
			height = format.height;
		}
		this.landscape = landscape;
		this.format = format;
	}

	public PaperSize(double width, double height) {
		this.width = width;
		this.height = height;
		format = null;
		landscape = width > height;
	}

	public Rectangle createRectangle() {
		return new Rectangle((float) width, (float) height);
	}
}
