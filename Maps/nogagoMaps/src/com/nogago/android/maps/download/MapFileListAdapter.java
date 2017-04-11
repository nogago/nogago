package com.nogago.android.maps.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.nogago.android.maps.Constants;
import com.nogago.android.maps.R;
import com.nogago.android.maps.download.task.readareas.Area;
import com.nogago.android.maps.download.task.readareas.ReadAreasTask;
import com.nogago.android.maps.plus.OsmandApplication;
import com.nogago.android.maps.plus.OsmandSettings;
import com.nogago.android.maps.plus.ResourceManager;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

/** Populates the items for displaying in UI */
public class MapFileListAdapter extends BaseAdapter implements ListAdapter{

	private final Context context;

	List<MapFile> mapFiles;

	public MapFileListAdapter(Context context) {
		this.context = context;
		readMapsFromDisk();
	}

	private void readMapsFromDisk() {
		File dir = new File(OsmandApplication.getSettings().extendOsmandPath(ResourceManager.APP_DIR).toString());
		File[] filelist = dir.listFiles();
		mapFiles = new ArrayList<MapFile>();
		for (int i = 0; i < filelist.length; i++) {
			if(filelist[i].getName().endsWith(Constants.MAP_FILE_EXTENSION)) {
				//Delete bad obf file
				if(filelist[i].length() < Constants.BAD_OBF_FILE_THRESHOLD) FileUtils.deleteQuietly(filelist[i]);
				else {
					try{
						mapFiles.add(new MapFile(filelist[i]));
					}catch(Exception e){
						//Other osmand files, ignore!
					}
				}
			}
		}
	}
	
	public void setMapFiles(List<MapFile> mapFiles) {
		this.mapFiles = mapFiles;
	}

	@Override
	public int getCount() {
		readMapsFromDisk();
		if(mapFiles == null) return 0;
		return mapFiles.size();
	}

	@Override
	public MapFile getItem(int pos) {
		if(mapFiles  == null) return null;
		return mapFiles.get(pos);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// if the view has not been created yet ...
		if (convertView == null) {
			// inflate the layout
			LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.find_entry, parent, false);
		}
		TextView entryView = (TextView) convertView.findViewById(R.id.findcity_entry_title);

		// get entry title
		MapFile item = (MapFile) getItem(position);
		// set the entry title
		entryView.setText(getItemDisplayName(item));

		return convertView;
	}
	
	public String getItemDisplayName(MapFile item){
		String title = item.getName();
		// if(item.part.compareTo("c")==0) title += " (" + context.getString(R.string.c) + ")";
		if(item.part.compareTo("nw")==0) title += " (" + context.getString(R.string.nw) + ")";
		if(item.part.compareTo("ne")==0) title += " (" + context.getString(R.string.ne) + ")";
		if(item.part.compareTo("sw")==0) title += " (" + context.getString(R.string.sw) + ")";
		if(item.part.compareTo("se")==0) title += " (" + context.getString(R.string.se) + ")";
		if(item.part.compareTo("s")==0) title += " (" + context.getString(R.string.s) + ")";
		if(item.part.compareTo("n")==0) title += " (" + context.getString(R.string.n) + ")";
		if(item.part.compareTo("w")==0) title += " (" + context.getString(R.string.w) + ")";
		if(item.part.compareTo("e")==0) title += " (" + context.getString(R.string.e) + ")";
		if(item.part2.compareTo("poly")==0) title += " (" + context.getString(R.string.contour) + ")";
//		if(item.part2.contains("c")) title += " (" + context.getString(R.string.contour) + ")";
		
		return title;
	}
	
	public List<MapFile> getMapFiles(){
		return this.mapFiles;
	}

	public Area getCityCenterArea(String cityName){
		for(MapFile mapFile: mapFiles){
			if(mapFile.fullName.startsWith(cityName + "-c")) return ReadAreasTask.getAreaById(mapFile.getMapId());
		}
		return null;
	}
	
	public MapFile getMapFileById(int mapId){
		for(MapFile mapFile: mapFiles){
			if(mapFile.getMapId() == mapId){
				return mapFile;
			}
		}
		return null;
	}
}
