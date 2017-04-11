package mobac.mapsources.mappacks.region_europe_east;

import java.awt.Color;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.model.TileImageType;

/**
 * 
 */
public class Turaterkep extends AbstractHttpMapSource {

	public Turaterkep() {
		super("Turaterkep256", 7, 15, TileImageType.PNG, TileUpdate.IfModifiedSince);
	}

	public String getTileUrl(int zoom, int x, int y) {
		return "http://a.map.turistautak.hu/tiles/turistautak-domborzattal/" + zoom + "/" + x + "/" + y + ".png";

	}

	@Override
	public String toString() {
		return "Turaterkep (Hungary)";
	}

	@Override
	public Color getBackgroundColor() {
		return Color.WHITE;
	}

}