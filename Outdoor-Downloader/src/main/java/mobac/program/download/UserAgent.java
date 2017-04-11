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
package mobac.program.download;

public class UserAgent {

	public static final String IE7_XP = "Mozilla/4.0 (compatible; MSIE 7.0; "
			+ "Windows NT 5.1; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1))";

	public static final String IE6_XP = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)";

	public static final String IE9_WIN7 = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)";

	public static final String FF2_XP = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en; rv:1.8.1.17) Gecko/20080829";

	public static final String FF3_XP = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en; rv:1.9.2) Gecko/20100115 Firefox/3.6";

	public static final String FF3_WIN7 = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en; rv:1.9.2.15) Gecko/20110303 Firefox/3.6.15";

	public static final String OPERA11_WIN7 = "Opera/9.80 (Windows NT 6.1; U; en) Presto/2.7.62 Version/11.01";

	private String name;
	private String userAgent;

	protected UserAgent(String name, String userAgent) {
		super();
		this.name = name;
		this.userAgent = userAgent;
	}

	public String getName() {
		return name;
	}

	public String getUserAgent() {
		return userAgent;
	}

	@Override
	public String toString() {
		return name;
	}

}
