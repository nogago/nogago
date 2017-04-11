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
package mobac.utilities;

public class MyMath {

	/**
	 * 
	 * Rounds down to the nearest multiple of <code>toNearest</code>
	 * 
	 * @param value
	 *            positive value
	 * @param toNearest
	 *            positive value
	 * @return
	 */
	public static int roundDownToNearest(int value, int toNearest) {
		int remainder = value % toNearest;
		value -= remainder;
		return value;
	}

	/**
	 * 
	 * Rounds up to the nearest multiple of <code>toNearest</code>
	 * 
	 * @param value
	 *            positive value
	 * @param toNearest
	 *            positive value
	 * @return
	 */
	public static int roundUpToNearest(int value, int toNearest) {
		int remainder = value % toNearest;
		if (remainder == 0)
			return value;
		value += (toNearest - remainder);
		return value;
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static int ceil(double value) {
		return (int) Math.ceil(value);
	}

	/**
	 * Divides <code>value</code> by <code>divisor</code> and performs the function "ceil" on the fractional result.
	 * <b>Warning: This method works only with positive input values!</b>
	 * <p>
	 * Internally only integer arithmetics is used (no floating point arithmetics)
	 * </p>
	 * 
	 * @param value
	 *            positive value
	 * @param divisor
	 *            positive value
	 * @return
	 */
	public static int divCeil(int value, int divisor) {
		int result = value / divisor;
		int remainder = value % divisor;
		if (remainder != 0)
			result++;
		return result;
	}

	/**
	 * Divides <code>value</code> by <code>divisor</code> and performs the function "ceil" on the fractional
	 * result.<b>Warning: This method works only with positive input values!</b>
	 * <p>
	 * Internally only integer arithmetics is used (no floating point arithmetics)
	 * </p>
	 * 
	 * @param value
	 *            positive value
	 * @param divisor
	 *            positive value
	 * @return
	 */
	public static long divCeil(long value, long divisor) {
		long result = value / divisor;
		if (value % divisor != 0)
			result++;
		return result;
	}

	/**
	 * Divides <code>value</code> by <code>divisor</code> and performs the function "round" on the fractional result.
	 * <b>Warning: This method works only with positive input values!</b>
	 * <p>
	 * Internally only integer arithmetics is used (no floating point arithmetics)
	 * </p>
	 * 
	 * @param value
	 *            positive value
	 * @param divisor
	 *            positive value
	 * @return
	 */
	public static int divRound(int value, int divisor) {
		int result = value / divisor;
		int remainder = value % divisor;
		if (remainder > (divisor >> 1))
			result++;
		return result;
	}

	/**
	 * Divides <code>value</code> by <code>divisor</code> and performs the function "round" on the fractional result.
	 * <b>Warning: This method works only with positive input values!</b>
	 * <p>
	 * Internally only integer arithmetics is used (no floating point arithmetics)
	 * </p>
	 * 
	 * @param value
	 *            positive value
	 * @param divisor
	 *            positive value
	 * @return
	 */
	public static long divRound(long value, long divisor) {
		long result = value / divisor;
		long remainder = value % divisor;
		if (remainder > (divisor >> 1))
			result++;
		return result;
	}

	/**
	 * Examples:
	 * 
	 * <pre>
	 *     0.1 to     1.0
	 *     0.5 to     1.0
	 *     0.9 to     1.0
	 *     1.1 to     1.0
	 *     1.9 to     2.0
	 *     9.9 to    10.0
	 *    15.5 to    20.0
	 *    88.0 to    90.0
	 *   363.0 to   400.0 
	 *  1155.0 to  1000.0
	 *  1655.0 to  2000.0
	 * 13455.0 to 10000.0
	 * 17755.0 to 20000.0
	 * </pre>
	 * 
	 * @param val
	 * @return array containing the rounded value
	 */
	public static double prettyRound(double val) {
		if (val < 1.0)
			return Math.ceil(val);
		int l10 = (int) Math.pow(10, Math.floor(Math.log10(val)));
		double x = val / l10;
		x = Math.round(x);
		x = x * l10;
		return x;
	}

}
