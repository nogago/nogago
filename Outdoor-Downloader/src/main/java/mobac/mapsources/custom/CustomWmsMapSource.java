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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import mobac.mapsources.MapSourceTools;

/**
 * Custom tile store provider for wms map sources, configurable via xml file
 * 
 * @author oruxman
 */
@XmlRootElement
public class CustomWmsMapSource extends CustomMapSource {

	/**
	 * tested with 1.1.1, but should work with other versions
	 */
	@XmlElement(required = true, name = "version")
	private String version = "1.1.1";

	/**
	 * no spaces allowed, must be replaced with %20 in the url
	 */
	@XmlElement(required = true, name = "layers")
	private String layers = "";

	/**
	 * currently only the coordinate system epsg:4326 is supported
	 */
	@XmlElement(required = true, name = "coordinatesystem", defaultValue = "EPSG:4326")
	private String coordinatesystem = "EPSG:4326";

	/**
	 * some wms needs more parameters: &amp;EXCEPTIONS=BLANK&amp;Styles= .....
	 */
	@XmlElement(required = false, name = "aditionalparameters")
	private String aditionalparameters = "";

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		double[] coords = MapSourceTools.calculateLatLon(this, zoom, tilex, tiley);
		String url = this.url + "REQUEST=GetMap" + "&LAYERS=" + layers + "&SRS=" + coordinatesystem + "&VERSION="
				+ version + "&FORMAT=image/" + tileType.getMimeType() + "&BBOX=" + coords[0] + "," + coords[1] + ","
				+ coords[2] + "," + coords[3] + "&WIDTH=256&HEIGHT=256" + aditionalparameters;
		return url;
	}

	public String getVersion() {
		return version;
	}

	public String getLayers() {
		return layers;
	}

	public String getCoordinatesystem() {
		return coordinatesystem;
	}

}
