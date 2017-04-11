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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import mobac.mapsources.AbstractMultiLayerMapSource;
import mobac.program.interfaces.MapSource;
import mobac.program.jaxb.ColorAdapter;
import mobac.program.model.TileImageType;

/**
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlSeeAlso({ CustomMapSource.class })
public class CustomMultiLayerMapSource extends AbstractMultiLayerMapSource {

	@XmlElementWrapper(name = "layers")
	@XmlElements({ @XmlElement(name = "customMapSource", type = CustomMapSource.class),
			@XmlElement(name = "mapSource", type = StandardMapSourceLayer.class),
			@XmlElement(name = "cloudMade", type = CustomCloudMade.class),
			@XmlElement(name = "localTileSQLite", type = CustomLocalTileSQliteMapSource.class),
			@XmlElement(name = "localTileFiles", type = CustomLocalTileFilesMapSource.class),
			@XmlElement(name = "localTileZip", type = CustomLocalTileZipMapSource.class) })
	protected List<CustomMapSource> layers = new ArrayList<CustomMapSource>();

	@XmlElement(defaultValue = "#000000")
	@XmlJavaTypeAdapter(ColorAdapter.class)
	protected Color backgroundColor = Color.BLACK;
	
	public CustomMultiLayerMapSource() {
		super();
		mapSources = new MapSource[0];
	}

	public TileImageType getTileType() {
		return tileType;
	}

	public void setTileType(TileImageType tileType) {
		this.tileType = tileType;
	}

	protected void afterUnmarshal(Unmarshaller u, Object parent) {
		mapSources = new MapSource[layers.size()];
		layers.toArray(mapSources);
		initializeValues();
	}

	@XmlElement(name = "name")
	public String getMLName() {
		return name;
	}

	public void setMLName(String name) {
		this.name = name;
	}

	@Override
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	

	// public static void main(String[] args) {
	// OutputStream os = null;
	// try {
	// JAXBContext context = JAXBContext.newInstance(new Class[] { CustomMultiLayerMapSource.class,
	// CustomMapSource.class });
	//
	// Marshaller marshaller = context.createMarshaller();
	// marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	// CustomMultiLayerMapSource ms = new CustomMultiLayerMapSource();
	// os = new FileOutputStream(new File("mapsources/Custom multi-layer map source.xml"));
	// marshaller.marshal(ms, os);
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// Utilities.closeStream(os);
	// }
	// }
	// public static void main(String[] args) {
	// try {
	// JAXBContext context = JAXBContext.newInstance(new Class[] { CustomMultiLayerMapSource.class,
	// CustomMapSource.class });
	//
	// Unmarshaller unmarshaller = context.createUnmarshaller();
	// CustomMultiLayerMapSource ms = (CustomMultiLayerMapSource) unmarshaller.unmarshal(new
	// File("mapsources/Example custom multi-layer map source.xml"));
	// System.out.println(ms);
	// System.out.println(ms.layers);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
}
