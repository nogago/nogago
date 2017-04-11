package mobac.mapsources;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import mobac.exceptions.MapSourceInitializationException;
import mobac.utilities.Utilities;
import mobac.utilities.writer.NullPrintWriter;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

public class MapSourceUrlUpdater {

	public static final String ACCEPT = " text/*, text/html, text/html;level=1";

	/**
	 * Loads the web page specified by <code>url</code>, parses it into DOM and extracts the <code>src</code> attribute
	 * of all <code>&lt;img&gt;</code> entities.
	 * 
	 * @param url
	 *            http or https url
	 * @param regex
	 * @return
	 * @throws IOException
	 */
	public static List<String> extractImgSrcList(String url, String regex) throws IOException {
		LinkedList<String> list = new LinkedList<String>();
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.addRequestProperty("Accept", ACCEPT);

		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			Utilities.getInputBytes(conn.getInputStream());
			throw new IOException("Invalid HTTP response code: " + conn.getResponseCode());
		}

		Tidy tidy = new Tidy();
		tidy.setErrout(new NullPrintWriter()); // Suppress error messages
		Document doc = tidy.parseDOM(conn.getInputStream(), null);

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr;
		NodeList nodes;
		try {
			expr = xpath.compile("//img[@src]");
			nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
		Pattern p = null;
		if (regex != null)
			p = Pattern.compile(regex);
		for (int i = 0; i < nodes.getLength(); i++) {
			String imgUrl = nodes.item(i).getAttributes().getNamedItem("src").getNodeValue();
			if (imgUrl != null && imgUrl.length() > 0) {
				if (p != null) {
					if (!p.matcher(imgUrl).matches())
						continue;
				}
				list.add(imgUrl);
			}
		}
		return list;
	}

	/**
	 * Retrieves the text or HTML document on the specified <code>url</code>, interprets the retrieved data as
	 * {@link String} of {@link Charset} <code>charset</code> and returns this {@link String}.
	 * 
	 * @param url
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static String loadDocument(String url, Charset charset) throws IOException {
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.addRequestProperty("Accept", ACCEPT);

		byte[] data = Utilities.getInputBytes(conn.getInputStream());
		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException("Invalid HTTP response code: " + conn.getResponseCode());
		}

		return new String(data, charset);
	}

	/**
	 * 
	 * @param url
	 * @param charset
	 * @param regex
	 *            regex defining one group with will be returned
	 * @return
	 * @throws MapSourceInitializationException
	 */
	public static String loadDocumentAndExtractGroup(String url, Charset charset, String regex)
			throws MapSourceInitializationException {
		String document;
		try {
			document = loadDocument(url, charset);
		} catch (IOException e) {
			throw new MapSourceInitializationException("Faile dto retrieve initialization document from url: " + url
					+ "\nError: " + e.getMessage(), e);
		}
		Matcher m = Pattern.compile(regex).matcher(document);
		if (!m.find())
			throw new MapSourceInitializationException("pattern not found: " + regex);
		return m.group(1);
	}

	public static void main(String[] args) {
		try {
			List<String> imgUrls = extractImgSrcList("http://maps.google.com/?ie=UTF8&ll=0,0&spn=0,0&z=2",
					"^http://mt\\d\\.google\\.com/.*");
			for (String s : imgUrls)
				System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
