package com.nogago.android.maps.download.task.readareas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

public class ReadAreasTask extends com.nogago.android.maps.download.task.TrackableTask {

	final static String FILE_NAME = "planet-latest-areas.list";
	final static int FILE_LINES = 2062; // Manually count when file changes
	static double minLatDelta = 360.0;
	static double minLonDelta = 360.0;
	
	Context ctx;
	private static List<Area> areas = null;

	public ReadAreasTask(String progressMessage, Context context) {
		super(progressMessage);
		ctx = context;
	}
	
	/** Allows to get the last read areas definition (based on the last task execution). When task is excuted the areas list is also returned to the caller onComplete */
	public static List<Area> getAreas() throws ReadAreasTaskException {
		if(areas !=null)
			return areas;
		throw new ReadAreasTaskException(null, ReadAreasTaskException.NOT_INITIALIZED);
	}

	/**
	 * Parser derived from uk.me.parabola.splitter.AreaList.read
	 * 
	 */
	@Override
	protected Object doInBackground(Object... arg0) {
		return loadAreaFile();
	}
	
	public Object loadAreaFile(){

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
				assetStream = this.ctx.getAssets().open(FILE_NAME);
				r = new InputStreamReader(assetStream);
				BufferedReader br = new BufferedReader(r);

				String line;
				while ((line = br.readLine()) != null) {
					i++;
					if (i % 500 == 0)
						publishProgress(((int) i * 100 / FILE_LINES));
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

					// Determine minimal offsets
					double myLatDelta = area.toDegrees( area.getMaxLat() - area.getMinLat() ); 
					double myLonDelta = area.toDegrees( area.getMaxLong() - area.getMinLong() );
					minLatDelta = (minLatDelta >= myLatDelta) ? myLatDelta : minLatDelta;
					minLonDelta = (minLonDelta >= myLonDelta) ? myLonDelta : minLonDelta;
				}
				// finalize offsets that are used when looking west and east / north and south of a given location
				minLatDelta = minLatDelta / 2.0;
				minLonDelta = minLonDelta / 2.0;
				br.close();
				return areas;
			} catch (NumberFormatException e) {
				return new ReadAreasTaskException(this, ReadAreasTaskException.NUMBER_FORMAT_EXCEPTION, e);
			} catch (FileNotFoundException e) {
				return new ReadAreasTaskException(this, ReadAreasTaskException.FILE_NOT_FOUND, e);
			} finally {
				if (r != null)
					r.close();
				if (assetStream != null)
					assetStream.close();
			}
		} catch (IOException e) {
			return new ReadAreasTaskException(this, ReadAreasTaskException.FILE_NOT_FOUND, e);
		}
	}

	/** The offset to use for looking north and south of a given point */
	public static double getMinLatDelta() {
		return minLatDelta;
	}

	/** The offset to use for looking west and east of a given point */
	public static double getMinLonDelta() {
		return minLonDelta;
	}
	
	public static Area getAreaById(int id){
		if(areas == null) return null;
		for(Area area: areas){
			if(area.getMapId() == id) return area;
		}
		return null;
	}

}
