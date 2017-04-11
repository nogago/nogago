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

import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * A JULI (java.util.logging) handler that redirects java.util.logging messages
 * to Log4J http://wiki.apache.org/myfaces/Trinidad_and_Common_Logging <br>
 * User: josh Date: Jun 4, 2008 Time: 3:31:21 PM
 * 
 * http://shrubbery.mynetgear.net/wiki/Routing_java.util.
 * logging_messages_to_Log4J
 */
public class Juli2Log4jHandler extends Handler {

	public void publish(LogRecord record) {
		org.apache.log4j.Logger log4j = getTargetLogger(record.getSourceClassName());
		Priority priority = toLog4j(record.getLevel());
		log4j.log(priority, toLog4jMessage(record), record.getThrown());
	}

	static Logger getTargetLogger(String loggerName) {
		return Logger.getLogger(loggerName);
	}

	public static Logger getTargetLogger(Class<?> clazz) {
		return getTargetLogger(clazz.getName());
	}

	private String toLog4jMessage(LogRecord record) {
		String message = record.getMessage();
		// Format message
		try {
			Object parameters[] = record.getParameters();
			if (parameters != null && parameters.length != 0) {
				// Check for the first few parameters ?
				if (message.indexOf("{0}") >= 0 || message.indexOf("{1}") >= 0
						|| message.indexOf("{2}") >= 0 || message.indexOf("{3}") >= 0) {
					message = MessageFormat.format(message, parameters);
				}
			}
		} catch (Exception ex) {
			// ignore Exception
		}
		return message;
	}

	private org.apache.log4j.Level toLog4j(Level level) {// converts levels
		if (Level.SEVERE == level) {
			return org.apache.log4j.Level.ERROR;
		} else if (Level.WARNING == level) {
			return org.apache.log4j.Level.WARN;
		} else if (Level.INFO == level) {
			return org.apache.log4j.Level.INFO;
		} else if (Level.OFF == level) {
			return org.apache.log4j.Level.OFF;
		}
		return org.apache.log4j.Level.OFF;
	}

	@Override
	public void flush() {
		// nothing to do
	}

	@Override
	public void close() {
		// nothing to do
	}
}
