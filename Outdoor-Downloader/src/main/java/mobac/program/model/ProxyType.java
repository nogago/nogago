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

public enum ProxyType {
	SYSTEM("Use standard Java proxy settings"), // 
	APP_SETTINGS("Use application properties"), //
	CUSTOM("Use custom proxy (user defined)"), //
	CUSTOM_W_AUTH("Use custom proxy with Authentication");

	private String text;

	private ProxyType(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}

}
