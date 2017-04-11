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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import mobac.Main;
import mobac.program.Logging;
import mobac.program.interfaces.MapSource;
import mobac.program.model.TileImageType;
import mobac.utilities.file.DirectoryFileFilter;

import org.apache.log4j.Logger;

public class Utilities {

	public static final Color COLOR_TRANSPARENT = new Color(0, 0, 0, 0);
	public static final DecimalFormatSymbols DFS_ENG = new DecimalFormatSymbols(Locale.ENGLISH);
	public static final DecimalFormatSymbols DFS_LOCAL = new DecimalFormatSymbols();
	public static final DecimalFormat FORMAT_6_DEC = new DecimalFormat("#0.######");
	public static final DecimalFormat FORMAT_6_DEC_ENG = new DecimalFormat("#0.######", DFS_ENG);
	public static final DecimalFormat FORMAT_2_DEC = new DecimalFormat("0.00");
	private static final DecimalFormat cDmsMinuteFormatter = new DecimalFormat("00");
	private static final DecimalFormat cDmsSecondFormatter = new DecimalFormat("00.0");

	private static final Logger log = Logger.getLogger(Utilities.class);

	public static final long SECONDS_PER_HOUR = TimeUnit.HOURS.toSeconds(1);
	public static final long SECONDS_PER_DAY = TimeUnit.DAYS.toSeconds(1);

	public static boolean testJaiColorQuantizerAvailable() {
		try {
			Class<?> c = Class.forName("javax.media.jai.operator.ColorQuantizerDescriptor");
			if (c != null)
				return true;
		} catch (NoClassDefFoundError e) {
			return false;
		} catch (Throwable t) {
			log.error("Error in testJaiColorQuantizerAvailable():", t);
			return false;
		}
		return true;
	}

	public static BufferedImage createEmptyTileImage(MapSource mapSource) {
		int tileSize = mapSource.getMapSpace().getTileSize();
		Color color = mapSource.getBackgroundColor();

		int imageType;
		if (color.getAlpha() == 255)
			imageType = BufferedImage.TYPE_INT_RGB;
		else
			imageType = BufferedImage.TYPE_INT_ARGB;
		BufferedImage emptyImage = new BufferedImage(tileSize, tileSize, imageType);
		Graphics2D g = emptyImage.createGraphics();
		try {
			g.setColor(color);
			g.fillRect(0, 0, tileSize, tileSize);
		} finally {
			g.dispose();
		}
		return emptyImage;
	}

	public static byte[] createEmptyTileData(MapSource mapSource) {
		BufferedImage emptyImage = createEmptyTileImage(mapSource);
		ByteArrayOutputStream buf = new ByteArrayOutputStream(4096);
		try {
			ImageIO.write(emptyImage, mapSource.getTileImageType().getFileExt(), buf);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		byte[] emptyTileData = buf.toByteArray();
		return emptyTileData;
	}

	private static final byte[] PNG = new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A };
	private static final byte[] JPG = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, (byte) 0x00,
			0x10, 'J', 'F', 'I', 'F' };
	private static final byte[] GIF_1 = "GIF87a".getBytes();
	private static final byte[] GIF_2 = "GIF89a".getBytes();

	public static TileImageType getImageType(byte[] imageData) {
		if (imageData == null)
			return null;
		if (startsWith(imageData, PNG))
			return TileImageType.PNG;
		if (startsWith(imageData, JPG))
			return TileImageType.JPG;
		if (startsWith(imageData, GIF_1) || startsWith(imageData, GIF_2))
			return TileImageType.GIF;
		return null;
	}

	public static boolean startsWith(byte[] data, byte[] startTest) {
		if (data.length < startTest.length)
			return false;
		for (int i = 0; i < startTest.length; i++)
			if (data[i] != startTest[i])
				return false;
		return true;
	}

	/**
	 * Checks if the available JAXB version is at least v2.1
	 */
	public static boolean checkJAXBVersion() {
		try {
			// We are trying to load the class javax.xml.bind.JAXB which has
			// been introduced with JAXB 2.1. Previous version do not contain
			// this class and will therefore throw an exception.
			Class<?> c = Class.forName("javax.xml.bind.JAXB");
			return (c != null);
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static InputStream loadResourceAsStream(String resourcePath) throws IOException {
		return Main.class.getResourceAsStream("resources/" + resourcePath);
	}

	public static String loadTextResource(String resourcePath) throws IOException {
		DataInputStream in = new DataInputStream(Main.class.getResourceAsStream("resources/" + resourcePath));
		byte[] buf;
		buf = new byte[in.available()];
		in.readFully(buf);
		in.close();
		String text = new String(buf, Charsets.UTF_8);
		return text;
	}

	/**
	 * 
	 * @param imageName
	 *            imagePath resource path relative to the class {@link Main}
	 * @return
	 */
	public static ImageIcon loadResourceImageIcon(String imageName) {
		URL url = Main.class.getResource("resources/images/" + imageName);
		return new ImageIcon(url);
	}

	public static URL getResourceImageUrl(String imageName) {
		return Main.class.getResource("resources/images/" + imageName);
	}

	public static void loadProperties(Properties p, URL url) throws IOException {
		InputStream propIn = url.openStream();
		try {
			p.load(propIn);
		} finally {
			closeStream(propIn);
		}
	}

	public static void loadProperties(Properties p, File f) throws IOException {
		InputStream propIn = new FileInputStream(f);
		try {
			p.load(propIn);
		} finally {
			closeStream(propIn);
		}
	}

	/**
	 * Checks if the current {@link Thread} has been interrupted and if so a {@link InterruptedException}. Therefore it
	 * behaves similar to {@link Thread#sleep(long)} without actually slowing down anything by sleeping a certain amount
	 * of time.
	 * 
	 * @throws InterruptedException
	 */
	public static void checkForInterruption() throws InterruptedException {
		if (Thread.currentThread().isInterrupted())
			throw new InterruptedException();
	}

	/**
	 * Checks if the current {@link Thread} has been interrupted and if so a {@link RuntimeException} will be thrown.
	 * This method is useful for long lasting operations that do not allow to throw an {@link InterruptedException}.
	 * 
	 * @throws RuntimeException
	 */
	public static void checkForInterruptionRt() throws RuntimeException {
		if (Thread.currentThread().isInterrupted())
			throw new RuntimeException(new InterruptedException());
	}

	public static void closeFile(RandomAccessFile file) {
		if (file == null)
			return;
		try {
			file.close();
		} catch (IOException e) {
		}
	}

	public static void closeStream(InputStream in) {
		if (in == null)
			return;
		try {
			in.close();
		} catch (IOException e) {
		}
	}

	public static void closeStream(OutputStream out) {
		if (out == null)
			return;
		try {
			out.close();
		} catch (IOException e) {
		}
	}

	public static void closeWriter(Writer writer) {
		if (writer == null)
			return;
		try {
			writer.close();
		} catch (IOException e) {
		}
	}

	public static void closeReader(OutputStream reader) {
		if (reader == null)
			return;
		try {
			reader.close();
		} catch (IOException e) {
		}
	}

	public static void closeStatement(Statement statement) {
		if (statement == null)
			return;
		try {
			statement.close();
		} catch (SQLException e) {
		}
	}

	public static double parseLocaleDouble(String text) throws ParseException {
		ParsePosition pos = new ParsePosition(0);
		Number n = Utilities.FORMAT_6_DEC.parse(text, pos);
		if (n == null)
			throw new ParseException("Unknown error", 0);
		if (pos.getIndex() != text.length())
			throw new ParseException("Text ends with unparsable characters", pos.getIndex());
		return n.doubleValue();
	}

	public static void showTooltipNow(JComponent c) {
		Action toolTipAction = c.getActionMap().get("postTip");
		if (toolTipAction != null) {
			ActionEvent postTip = new ActionEvent(c, ActionEvent.ACTION_PERFORMED, "");
			toolTipAction.actionPerformed(postTip);
		}
	}

	/**
	 * Formats a byte value depending on the size to "Bytes", "KiBytes", "MiByte" and "GiByte"
	 * 
	 * @param bytes
	 * @return Formatted {@link String}
	 */
	public static String formatBytes(long bytes) {
		if (bytes < 1000)
			return Long.toString(bytes) + " Bytes";
		if (bytes < 1000000)
			return FORMAT_2_DEC.format(bytes / 1024d) + " KiByte";
		if (bytes < 1000000000)
			return FORMAT_2_DEC.format(bytes / 1048576d) + " MiByte";
		return FORMAT_2_DEC.format(bytes / 1073741824d) + " GiByte";
	}

	public static String formatDurationSeconds(long seconds) {
		long x = seconds;
		long days = x / SECONDS_PER_DAY;
		x %= SECONDS_PER_DAY;
		int years = (int) (days / 365);
		days -= (years * 365);

		int months = (int) (days * 12d / 365d);
		String m = (months == 1) ? "month" : "months";

		if (years > 5)
			return String.format("%d years", years);
		if (years > 0) {
			String y = (years == 1) ? "year" : "years";
			return String.format("%d %s %d %s", years, y, months, m);
		}
		String d = (days == 1) ? "day" : "days";
		if (months > 0) {
			days -= months * (365d / 12d);
			return String.format("%d %s %d %s", months, m, days, d);
		}
		long hours = TimeUnit.SECONDS.toHours(x);
		String h = (hours == 1) ? "hour" : "hours";
		x -= hours * SECONDS_PER_HOUR;
		if (days > 0)
			return String.format("%d %s %d %s", days, d, hours, h);
		long minutes = TimeUnit.SECONDS.toMinutes(x);
		String min = (minutes == 1) ? "minute" : "minutes";
		if (hours > 0)
			return String.format("%d %s %d %s", hours, h, minutes, min);
		else
			return String.format("%d %s", minutes, min);
	}

	public static void mkDir(File dir) throws IOException {
		if (dir.isDirectory())
			return;
		if (!dir.mkdir())
			throw new IOException("Failed to create directory \"" + dir.getAbsolutePath() + "\"");
	}

	public static void mkDirs(File dir) throws IOException {
		if (dir.isDirectory())
			return;
		if (dir.mkdirs())
			return;

		if (Logging.isCONFIGURED())
			Logging.LOG.error("mkDirs creation failed first time - one retry left");

		// Wait some time and then retry it.
		// See for details:
		// http://javabyexample.wisdomplug.com/component/content/article/37-core-java/48-is-mkdirs-thread-safe.html
		// Hopefully this will fix the different bugs reported for this method
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		if (dir.mkdirs())
			return;

		throw new IOException("Failed to create directory \"" + dir.getAbsolutePath() + "\"");
	}

	public static void fileCopy(File sourceFile, File destFile) throws IOException {

		FileChannel source = null;
		FileChannel destination = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;

		try {
			fis = new FileInputStream(sourceFile);
			fos = new FileOutputStream(destFile);

			source = fis.getChannel();
			destination = fos.getChannel();

			destination.transferFrom(source, 0, source.size());
		} finally {
			fis.close();
			fos.close();

			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	public static byte[] getFileBytes(File file) throws IOException {
		int size = (int) file.length();
		byte[] buffer = new byte[size];
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		try {
			in.readFully(buffer);
			return buffer;
		} finally {
			closeStream(in);
		}
	}

	/**
	 * Fully reads data from <tt>in</tt> to an internal buffer until the end of in has been reached. Then the buffer is
	 * returned.
	 * 
	 * @param in
	 *            data source to be read
	 * @return buffer all data available in in
	 * @throws IOException
	 */
	public static byte[] getInputBytes(InputStream in) throws IOException {
		int initialBufferSize = in.available();
		if (initialBufferSize <= 0)
			initialBufferSize = 32768;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(initialBufferSize);
		byte[] b = new byte[1024];
		int ret = 0;
		while ((ret = in.read(b)) >= 0) {
			buffer.write(b, 0, ret);
		}
		return buffer.toByteArray();
	}

	/**
	 * Fully reads data from <tt>in</tt> the read data is discarded.
	 * 
	 * @param in
	 * @throws IOException
	 */
	public static void readFully(InputStream in) throws IOException {
		byte[] b = new byte[4096];
		while ((in.read(b)) >= 0) {
		}
	}

	/**
	 * Lists all direct sub directories of <code>dir</code>
	 * 
	 * @param dir
	 * @return list of directories
	 */
	public static File[] listSubDirectories(File dir) {
		return dir.listFiles(new DirectoryFileFilter());
	}

	public static List<File> listSubDirectoriesRec(File dir, int maxDepth) {
		List<File> dirList = new LinkedList<File>();
		addSubDirectories(dirList, dir, maxDepth);
		return dirList;
	}

	public static void addSubDirectories(List<File> dirList, File dir, int maxDepth) {
		File[] subDirs = dir.listFiles(new DirectoryFileFilter());
		for (File f : subDirs) {
			dirList.add(f);
			if (maxDepth > 0)
				addSubDirectories(dirList, f, maxDepth - 1);
		}
	}

	public static String prettyPrintLatLon(double coord, boolean isCoordKindLat) {
		boolean neg = coord < 0.0;
		String c;
		if (isCoordKindLat) {
			c = (neg ? "S" : "N");
		} else {
			c = (neg ? "W" : "E");
		}
		double tAbsCoord = Math.abs(coord);
		int tDegree = (int) tAbsCoord;
		double tTmpMinutes = (tAbsCoord - tDegree) * 60;
		int tMinutes = (int) tTmpMinutes;
		double tSeconds = (tTmpMinutes - tMinutes) * 60;
		return c + tDegree + "\u00B0" + cDmsMinuteFormatter.format(tMinutes) + "\'"
				+ cDmsSecondFormatter.format(tSeconds) + "\"";
	}

	public static void setHttpProxyHost(String host) {
		if (host != null && host.length() > 0)
			System.setProperty("http.proxyHost", host);
		else
			System.getProperties().remove("http.proxyHost");
	}

	public static void setHttpProxyPort(String port) {
		if (port != null && port.length() > 0)
			System.setProperty("http.proxyPort", port);
		else
			System.getProperties().remove("http.proxyPort");
	}

	/**
	 * Returns the file path for the selected class. If the class is located inside a JAR file the return value contains
	 * the directory that contains the JAR file. If the class file is executed outside of an JAR the root directory
	 * holding the class/package structure is returned.
	 * 
	 * @param mainClass
	 * @return
	 * @throws URISyntaxException
	 */
	public static File getClassLocation(Class<?> mainClass) {
		ProtectionDomain pDomain = mainClass.getProtectionDomain();
		CodeSource cSource = pDomain.getCodeSource();
		File f;
		try {
			URL loc = cSource.getLocation(); // file:/c:/almanac14/examples/
			f = new File(loc.toURI());
		} catch (Exception e) {
			throw new RuntimeException("Unable to determine program directory: ", e);
		}
		if (f.isDirectory()) {
			// Class is executed from class/package structure from file system
			return f;
		} else {
			// Class is executed from inside of a JAR -> f references the JAR
			// file
			return f.getParentFile();
		}
	}

	/**
	 * Saves <code>data</code> to the file specified by <code>filename</code>.
	 * 
	 * @param filename
	 * @param data
	 * @throws IOException
	 */
	public static void saveBytes(String filename, byte[] data) throws IOException {
		FileOutputStream fo = null;
		try {
			fo = new FileOutputStream(filename);
			fo.write(data);
		} finally {
			closeStream(fo);
		}
	}

	/**
	 * Saves <code>data</code> to the file specified by <code>filename</code>.
	 * 
	 * @param filename
	 * @param data
	 * @return Data has been saved successfully?
	 */
	public static boolean saveBytesEx(String filename, byte[] data) {
		FileOutputStream fo = null;
		try {
			fo = new FileOutputStream(filename);
			fo.write(data);
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			closeStream(fo);
		}
	}

	/**
	 * Tries to delete a file or directory and throws an {@link IOException} if that fails.
	 * 
	 * @param fileToDelete
	 * @throws IOException
	 *             Thrown if <code>fileToDelete</code> can not be deleted.
	 */
	public static void deleteFile(File fileToDelete) throws IOException {
		if (!fileToDelete.delete())
			throw new IOException("Deleting of \"" + fileToDelete + "\" failed.");
	}

	public static void renameFile(File oldFile, File newFile) throws IOException {
		if (!oldFile.renameTo(newFile))
			throw new IOException("Failed to rename file: " + oldFile + " to " + newFile);
	}

	public static int getJavaMaxHeapMB() {
		try {
			return (int) (Runtime.getRuntime().maxMemory() / 1048576l);
		} catch (Exception e) {
			return -1;
		}
	}

	public static byte[] downloadHttpFile(String url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		int responseCode = conn.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK)
			throw new IOException("Invalid HTTP response: " + responseCode + " for url " + conn.getURL());
		InputStream in = conn.getInputStream();
		try {
			return Utilities.getInputBytes(in);
		} finally {
			in.close();
		}
	}

	public static void copyFile(File source, File target) throws IOException {
		FileChannel in = (new FileInputStream(source)).getChannel();
		FileChannel out = (new FileOutputStream(target)).getChannel();
		in.transferTo(0, source.length(), out);
		in.close();
		out.close();
	}

	/**
	 * 
	 * @param value
	 *            positive value
	 * @return 0 if no bit is set else the highest bit that is one in <code>value</code>
	 */
	public static int getHighestBitSet(int value) {
		int bit = 0x40000000;
		for (int i = 31; i > 0; i--) {
			int test = bit & value;
			if (test != 0)
				return i;
			bit >>= 1;
		}
		return 0;
	}

	/**
	 * 
	 * @param revsision
	 *            SVN revision string like <code>"1223"</code>, <code>"1224M"</code> or <code>"1616:1622M"</code>
	 * @return parsed svn revision
	 */
	public static int parseSVNRevision(String revision) {
		revision = revision.trim();
		int index = revision.lastIndexOf(':');
		if (index >= 0) {
			revision = revision.substring(index + 1).trim();
		}
		Matcher m = Pattern.compile("(\\d+)[^\\d]*").matcher(revision);
		if (!m.matches())
			return -1;
		return Integer.parseInt(m.group(1));
	}

}
