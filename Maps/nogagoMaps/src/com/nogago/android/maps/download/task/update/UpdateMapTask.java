package com.nogago.android.maps.download.task.update;

import com.nogago.android.maps.UrlUtils;
import com.nogago.android.maps.download.MapFile;
import com.nogago.android.maps.download.task.download.DownloadTask;
import com.nogago.android.maps.download.task.readareas.Area;
import com.nogago.android.maps.download.task.readareas.ReadAreasTask;

/**
 * Task to update selected map on SD card.
 * 
 * @author Tian Bai
 * 
 */
public class UpdateMapTask extends DownloadTask{
	
	MapFile mapFile;
	Area area;
	
	public UpdateMapTask(String progressMessage, String user, String passwd, MapFile mapFile) {
		super(progressMessage, UrlUtils.getMapUrl(mapFile.getMapId()), user, passwd, mapFile.getFullPath());
		setPartSize(2);
		this.setFilePath(mapFile.getFullPath());
		this.mapFile = mapFile;
		this.area = ReadAreasTask.getAreaById(mapFile.getMapId());
	}
	
	/*
	 * First, new file will be downloaded to a temp dir, then replace the old one.
	 * (non-Javadoc)
	 * @see com.nogago.android.task.download.DownloadTask#doInBackground(java.lang.Object[])
	 */
	@Override
	protected Object doInBackground(Object... arg0) {
		//Update Map
		Object object = super.doInBackground(1, arg0);
		//There is some exception during downloading
		if(!(object instanceof Boolean) || !(Boolean)object){
			return object;
		}
		
		//Update POI
/*
		this.setUrl(area.getPoiUrl());
		this.setFilePath(mapFile.getPoiPath());
*/
		
		return super.doInBackground(2, arg0);
		
	}

}
