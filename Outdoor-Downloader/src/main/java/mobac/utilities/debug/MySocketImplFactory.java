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
package mobac.utilities.debug;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketImplFactory;
import java.net.URL;

import mobac.program.Logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Proxies the default {@link SocketImpl} using reflection. This allows to trace
 * all calls into the log file.
 * 
 * Requires SunJRE/SunJDK!
 */
public class MySocketImplFactory implements SocketImplFactory {

	private static Logger log = Logger.getLogger(MySocketImplFactory.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			install();
			Logging.configureConsoleLogging(Level.TRACE);

			HttpURLConnection conn = (HttpURLConnection) new URL("http://google.de")
					.openConnection();
			conn.connect();
			byte[] data = new byte[1024];
			new DataInputStream(conn.getInputStream()).readFully(data);
			System.out.println(new String(data));
			Thread.sleep(1000);
			System.gc();
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void install() throws IOException {
		try {
			Socket.setSocketImplFactory(new MySocketImplFactory());
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("Unable to install " + MySocketImplFactory.class.getSimpleName(),
					e);
		}
	}

	private final Constructor<?> constructor;
	private final Method accept;
	private final Method bind;
	private final Method available;
	private final Method create;
	private final Method connect1;
	private final Method connect2;
	private final Method connect3;
	private final Method close;
	private final Method getInputStream;
	private final Method getOutputStream;
	private final Method sendUrgentData;
	private final Method listen;

	public MySocketImplFactory() throws ClassNotFoundException, SecurityException,
			NoSuchMethodException {
		super();
		Class<?> c = Class.forName("java.net.PlainSocketImpl");
		constructor = c.getDeclaredConstructor();
		constructor.setAccessible(true);
		accept = c.getDeclaredMethod("accept", SocketImpl.class);
		accept.setAccessible(true);
		bind = c.getDeclaredMethod("bind", InetAddress.class, Integer.TYPE);
		bind.setAccessible(true);
		available = c.getDeclaredMethod("available");
		available.setAccessible(true);
		create = c.getDeclaredMethod("create", Boolean.TYPE);
		create.setAccessible(true);
		connect1 = c.getDeclaredMethod("connect", InetAddress.class, Integer.TYPE);
		connect1.setAccessible(true);
		connect2 = c.getDeclaredMethod("connect", SocketAddress.class, Integer.TYPE);
		connect2.setAccessible(true);
		connect3 = c.getDeclaredMethod("connect", String.class, Integer.TYPE);
		connect3.setAccessible(true);
		getInputStream = c.getDeclaredMethod("getInputStream");
		getInputStream.setAccessible(true);
		getOutputStream = c.getDeclaredMethod("getOutputStream");
		getOutputStream.setAccessible(true);
		close = c.getDeclaredMethod("close");
		close.setAccessible(true);
		sendUrgentData = c.getDeclaredMethod("sendUrgentData", Integer.TYPE);
		sendUrgentData.setAccessible(true);
		listen = c.getDeclaredMethod("listen", Integer.TYPE);
		listen.setAccessible(true);
	}

	public SocketImpl createSocketImpl() {
		try {
			return new MySocketImpl();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private class MySocketImpl extends SocketImpl {

		private final SocketImpl si;

		private final int socketId;

		private MySocketImpl() throws IllegalArgumentException, InstantiationException,
				IllegalAccessException, InvocationTargetException {
			si = (SocketImpl) constructor.newInstance();
			socketId = si.hashCode();
			log.trace("[" + socketId + "] new SocketImpl created");
		}

		@Override
		protected void finalize() throws Throwable {
			close();
			super.finalize();
		}

		@Override
		protected void accept(SocketImpl s) throws IOException {
			log.trace("[" + socketId + "] accept(...)");
			try {
				accept.invoke(si, s);
			} catch (Exception e) {
				if (e instanceof IOException)
					throw (IOException) e;
				throw new RuntimeException(e);
			}
		}

		@Override
		protected int available() throws IOException {
			// log.trace("[" + socketId + "] available()");
			try {
				return ((Integer) bind.invoke(si)).intValue();
			} catch (Exception e) {
				if (e instanceof IOException)
					throw (IOException) e;
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void bind(InetAddress host, int port) throws IOException {
			log.trace("[" + socketId + "] bind()");
			try {
				bind.invoke(si, host, port);
			} catch (Exception e) {
				if (e instanceof IOException)
					throw (IOException) e;
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void close() throws IOException {
			log.trace("[" + socketId + "] close()");
			try {
				close.invoke(si);
			} catch (Exception e) {
				if (e instanceof IOException)
					throw (IOException) e;
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void connect(InetAddress address, int port) throws IOException {
			log.trace("[" + socketId + "] connect1(..)");
			try {
				connect1.invoke(si, address, port);
			} catch (Exception e) {
				if (e instanceof IOException)
					throw (IOException) e;
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void connect(SocketAddress address, int timeout) throws IOException {
			log.trace("[" + socketId + "] connect2(..)");
			try {
				connect2.invoke(si, address, timeout);
			} catch (Exception e) {
				if (e instanceof IOException)
					throw (IOException) e;
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void connect(String host, int port) throws IOException {
			log.trace("[" + socketId + "] connect3(..)");
			try {
				connect3.invoke(si, host, port);
			} catch (Exception e) {
				if (e instanceof IOException)
					throw (IOException) e;
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void create(boolean stream) throws IOException {
			log.trace("[" + socketId + "] create(..)");
			try {
				create.invoke(si, stream);
			} catch (Exception e) {
				if (e instanceof IOException)
					throw (IOException) e;
				throw new RuntimeException(e);
			}
		}

		@Override
		protected InputStream getInputStream() throws IOException {
			// log.trace("[" + socketId + "] getInputStream()");
			try {
				return (InputStream) getInputStream.invoke(si);
			} catch (Exception e) {
				if (e instanceof IOException)
					throw (IOException) e;
				throw new RuntimeException(e);
			}
		}

		public Object getOption(int optID) throws SocketException {
			// log.trace("[" + socketId + "] getOption(..)");
			return si.getOption(optID);
		}

		@Override
		protected OutputStream getOutputStream() throws IOException {
			// log.trace("[" + socketId + "] getOutputStream()");
			try {
				return (OutputStream) getOutputStream.invoke(si);
			} catch (Exception e) {
				if (e instanceof IOException)
					throw (IOException) e;
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void listen(int backlog) throws IOException {
			log.trace("[" + socketId + "] listen(..)");
			try {
				listen.invoke(si, backlog);
			} catch (Exception e) {
				if (e instanceof IOException)
					throw (IOException) e;
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void sendUrgentData(int data) throws IOException {
			// log.trace("[" + socketId + "] sendUrgentData");
			try {
				sendUrgentData.invoke(si, data);
			} catch (Exception e) {
				if (e instanceof IOException)
					throw (IOException) e;
				throw new RuntimeException(e);
			}
		}

		public void setOption(int optID, Object value) throws SocketException {
			// log.trace("[" + socketId + "] setOption");
			si.setOption(optID, value);
		}

	}
}
