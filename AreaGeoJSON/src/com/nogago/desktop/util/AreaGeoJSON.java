package com.nogago.desktop.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prepare Area List fo Web Display using Leaflet and GeoJSON following tutorial
 * at
 * http://palewi.re/posts/2012/03/26/leaflet-recipe-hover-events-features-and-
 * polygons/
 */
public class AreaGeoJSON {
	ArrayList<Area> areas;
	String fileName;

	AreaGeoJSON(String fileName) {
		this.fileName = fileName;
		read();
	}

	void read() {

		// DEBUG START
		// DEBUG END
		int i = 0;
		Reader r = null;
		InputStream assetStream = null;
		areas = new ArrayList<Area>();

		Pattern pattern = Pattern.compile("([0-9]{8}):"
				+ " ([\\p{XDigit}x-]+),([\\p{XDigit}x-]+)"
				+ " to ([\\p{XDigit}x-]+),([\\p{XDigit}x-]+)");
		try {
			try {

				BufferedReader br = new BufferedReader(new FileReader(fileName));

				String line;
				while ((line = br.readLine()) != null) {
					i++;

					line = line.trim();
					if (line.length() == 0 || line.charAt(0) == '#')
						continue;

					Matcher matcher = pattern.matcher(line);
					matcher.find();
					String mapid = matcher.group(1);

					Area area = new Area(Integer.decode(matcher.group(2)),
							Integer.decode(matcher.group(3)),
							Integer.decode(matcher.group(4)),
							Integer.decode(matcher.group(5)));
					area.setMapId(Integer.parseInt(mapid));
					areas.add(area);
				}
				// finalize offsets that are used when looking west and east /
				// north and south of a given location
				br.close();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (r != null)
					r.close();
				if (assetStream != null)
					assetStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void write(String outFile) {
		StringBuffer b = new StringBuffer();
		b.append("{ \"type\": \"FeatureCollection\", \"features\" [\n"); // GeoJSON Header
		for (Iterator<Area> iterator = areas.iterator(); iterator.hasNext();) {
			Area a = (Area) iterator.next();
			b.append(a.toGeoJSON());
			if(iterator.hasNext()) b.append(",\n");
		}
		b.append("\n ] }");
		try {
			  BufferedWriter out = new BufferedWriter(
			                       new FileWriter(outFile));
			  String outText = b.toString();
			  out.write(outText);
			  out.close();
			    }
			catch (IOException e)
			    {
			    e.printStackTrace();
			    }
	}

	public static void main(String args[]) {
		// args[0] must be link to the areas list
		if(true) { // (args.length < 2) {
			AreaGeoJSON c = new AreaGeoJSON("C:\\Users\\PC\\android-ws\\nogago-open-source\\Maps\\nogagoMaps\\assets\\planet-latest-areas.list");
			c.write("C:\\Users\\PC\\android-ws\\nogago-open-source\\Maps\\nogagoMaps\\assets\\list.geojson");
		} else {
			System.out
					.println("Usage: AreaGeoJSON <InputfileName> <OutputFileName>");
		}

	}
}
