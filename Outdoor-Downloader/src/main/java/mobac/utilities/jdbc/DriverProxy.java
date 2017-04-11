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
package mobac.utilities.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Proxies all calls to {@link Driver} that has been loaded using a custom {@link ClassLoader}. This is necessary as the
 * SQL {@link DriverManager} does only accept drivers loaded by the <code>SystemClassLoader</code>.
 */
public class DriverProxy implements Driver {

	private static Logger log = Logger.getLogger(DriverProxy.class);

	private final Driver driver;

	@SuppressWarnings("unchecked")
	public DriverProxy(String className, ClassLoader classLoader) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Class<Driver> c = (Class<Driver>) classLoader.loadClass(className);
		driver = c.newInstance();
		log.info("SQL driver loaded: v" + driver.getMajorVersion() + "." + driver.getMinorVersion() + " ["
				+ driver.getClass().getName() + "]");
	}

	public static void loadSQLDriver(String className, ClassLoader classLoader) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, SQLException {
		DriverProxy driver = new DriverProxy(className, classLoader);
		DriverManager.registerDriver(driver);
	}

	public boolean acceptsURL(String url) throws SQLException {
		return driver.acceptsURL(url);
	}

	public Connection connect(String url, Properties info) throws SQLException {
		return driver.connect(url, info);
	}

	public int getMajorVersion() {
		return driver.getMajorVersion();
	}

	public int getMinorVersion() {
		return driver.getMinorVersion();
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		return driver.getPropertyInfo(url, info);
	}

	public boolean jdbcCompliant() {
		return driver.jdbcCompliant();
	}

	/**
	 * Required for Java 7
	 */
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}

}
