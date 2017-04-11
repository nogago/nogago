package com.nogago.android.maps.download.task.download;

import java.io.File;

import com.nogago.android.maps.download.MapFile;
import com.nogago.android.maps.download.task.TrackableTask;

import org.apache.commons.io.FileUtils;

/**
 * Task to delete selected map on SD card.
 * 
 * @author Tian Bai
 * 
 */
public class DeleteMapTask extends TrackableTask {
	
	
	private MapFile mapFile;

	public DeleteMapTask(String progressMessage, MapFile mapFile) {
		super(progressMessage);
		this.mapFile = mapFile;
	}
	
	/* Separate Thread */
	@Override
	protected Object doInBackground(Object... arg0) {
		publishProgress(0);
		FileUtils.deleteQuietly(new File(mapFile.getFullPath()));
//		publishProgress(50);
//		FileUtils.deleteQuietly(new File(mapFile.getPoiPath()));
		publishProgress(100);
		return new Boolean(true);
	}

}
