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
package mobac.mapsources.custom;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import mobac.exceptions.UnrecoverableDownloadException;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.download.TileDownLoader;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.MapSourceListener;
import mobac.program.interfaces.MapSpace;
import mobac.program.jaxb.ColorAdapter;
import mobac.program.model.MapSourceLoaderInfo;
import mobac.program.model.TileImageType;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.TileStoreEntry;

/**
 * Custom tile store provider, configurable via settings.xml.
 */
@XmlRootElement
public class CustomMapSource implements HttpMapSource {

	@XmlElement(nillable = false, defaultValue = "Custom")
	private String name = "Custom";

	@XmlElement(defaultValue = "0")
	private int minZoom = 0;

	@XmlElement(required = true)
	private int maxZoom = 0;

	@XmlElement(defaultValue = "PNG")
	protected TileImageType tileType = TileImageType.PNG;

	@XmlElement(defaultValue = "NONE")
	private HttpMapSource.TileUpdate tileUpdate;

	@XmlElement(required = true, nillable = false)
	protected String url = "http://127.0.0.1/{$x}_{$y}_{$z}";

	@XmlElement(defaultValue = "#000000")
	@XmlJavaTypeAdapter(ColorAdapter.class)
	private Color backgroundColor = Color.BLACK;

	@XmlElement(required = false, defaultValue = "false")
	private boolean ignoreErrors = false;

	@XmlElement(required = false, defaultValue = "")
	@XmlList
	private String[] serverParts = null;
	private int currentServerPart = 0;

	private MapSourceLoaderInfo loaderInfo = null;

	/**
	 * Constructor without parameters - required by JAXB
	 */
	protected CustomMapSource() {
	}

	public CustomMapSource(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public TileUpdate getTileUpdate() {
		return tileUpdate;
	}

	public int getMaxZoom() {
		return maxZoom;
	}

	public int getMinZoom() {
		return minZoom;
	}

	public String getName() {
		return name;
	}

	public String getStoreName() {
		return name;
	}

	public TileImageType getTileImageType() {
		return tileType;
	}

	public HttpURLConnection getTileUrlConnection(int zoom, int tilex, int tiley) throws IOException {
		String url = getTileUrl(zoom, tilex, tiley);
		if (url == null)
			return null;
		return (HttpURLConnection) new URL(url).openConnection();
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		if (serverParts == null || serverParts.length == 0) {
			return MapSourceTools.formatMapUrl(url, zoom, tilex, tiley);
		} else {
			currentServerPart = (currentServerPart + 1) % serverParts.length;
			String serverPart = serverParts[currentServerPart];
			return MapSourceTools.formatMapUrl(url, serverPart, zoom, tilex, tiley);
		}
	}

	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			UnrecoverableDownloadException, InterruptedException {
		if (loadMethod == LoadMethod.CACHE) {
			TileStoreEntry entry = TileStore.getInstance().getTile(x, y, zoom, this);
			if (entry == null)
				return null;
			byte[] data = entry.getData();
			if (Thread.currentThread() instanceof MapSourceListener) {
				((MapSourceListener) Thread.currentThread()).tileDownloaded(data.length);
			}
			return data;
		}
		if (!ignoreErrors)
			return TileDownLoader.getImage(x, y, zoom, this);
		else
			try {
				return TileDownLoader.getImage(x, y, zoom, this);
			} catch (Exception e) {
				return null;
			}
	}

	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			UnrecoverableDownloadException, InterruptedException {
		byte[] data = getTileData(zoom, x, y, loadMethod);
		if (data == null) {
			if (!ignoreErrors)
				return null;
			else {
				BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_4BYTE_ABGR);
				Graphics g = (Graphics) image.getGraphics();
				try {
					g.setColor(backgroundColor);
					g.fillRect(0, 0, 256, 256);
				} finally {
					g.dispose();
				}
				return image;
			}
		}
		return ImageIO.read(new ByteArrayInputStream(data));
	}

	@Override
	public String toString() {
		return name;
	}

	public MapSpace getMapSpace() {
		return MercatorPower2MapSpace.INSTANCE_256;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	@XmlTransient
	public MapSourceLoaderInfo getLoaderInfo() {
		return loaderInfo;
	}

	public void setLoaderInfo(MapSourceLoaderInfo loaderInfo) {
		if (this.loaderInfo != null)
			throw new RuntimeException("LoaderInfo already set");
		this.loaderInfo = loaderInfo;
	}

}
