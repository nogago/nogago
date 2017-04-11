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

public class Coordinate {

	public static final int MILISECOND = 1, SECOND = MILISECOND * 1000, MINUTE = SECOND * 60, DEGREE = MINUTE * 60;

	public static int doubleToInt(double value) {
		int degree = (int) value;
		int minute = (int) (value = (value - degree) * 60d);
		int second = (int) (value = (value - minute) * 60d);
		int milisecond = (int) (value = (value - second) * 1000d);
		return degree * DEGREE + minute * MINUTE + second * SECOND + milisecond * MILISECOND;
	}

	public static double intToDouble(int value) {
		double degree = value / DEGREE;
		double minute = (value = value % DEGREE) / MINUTE;
		double second = (int) (value %= MINUTE) / SECOND;
		double milisecond = (int) (value %= SECOND) / MILISECOND;
		return degree + minute / 60d + second / 3600d + milisecond / 3600000d;
	}

	public static int getDegree(int value) {
		return value / DEGREE;
	}

	public static int getMinute(int value) {
		return Math.abs(value) % DEGREE / MINUTE;
	}

	public static int getSecond(int value) {
		return Math.abs(value) % MINUTE / SECOND;
	}

	public static int getMilisecond(int value) {
		return Math.abs(value) % SECOND / MILISECOND;
	}
}
