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
package unittests.methods;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import mobac.utilities.MyMath;

public class MyMathTests extends TestCase {

	public void testRoundDownToNearest() {
		assertEquals(0, MyMath.roundDownToNearest(0, 10));
		assertEquals(0, MyMath.roundDownToNearest(1, 10));
		assertEquals(0, MyMath.roundDownToNearest(9, 10));
		assertEquals(10, MyMath.roundDownToNearest(10, 10));
		assertEquals(10, MyMath.roundDownToNearest(11, 10));
		assertEquals(12340, MyMath.roundDownToNearest(12345, 10));
		assertEquals(1024, MyMath.roundDownToNearest(1025, 16));
		assertEquals(1024, MyMath.roundDownToNearest(1024, 256));
		assertEquals(1024, MyMath.roundDownToNearest(1025, 256));
	}

	public void testRoundUpToNearest() {
		assertEquals(0, MyMath.roundUpToNearest(0, 10));
		assertEquals(10, MyMath.roundUpToNearest(1, 10));
		assertEquals(10, MyMath.roundUpToNearest(9, 10));
		assertEquals(20, MyMath.roundUpToNearest(11, 10));
		assertEquals(12350, MyMath.roundUpToNearest(12345, 10));
		assertEquals(1040, MyMath.roundUpToNearest(1025, 16));
		assertEquals(1024, MyMath.roundUpToNearest(1024, 256));
		assertEquals(1280, MyMath.roundUpToNearest(1025, 256));
	}

	public static void main(String[] args) {
		TestRunner.run(MyMathTests.class);
	}

}
