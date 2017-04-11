package com.nogago.android.maps.download.task.download;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import com.nogago.android.maps.Constants;
import com.nogago.android.maps.download.task.TrackableTask;
import com.nogago.android.maps.download.task.readareas.Area;
import com.nogago.android.maps.plus.OsmandApplication;
import com.nogago.android.maps.plus.OsmandSettings;
import com.nogago.android.maps.plus.ResourceManager;

import android.os.Environment;

/**
 * Task to download from download.nogago.com via HTTPS authenticated
 * credentials.
 * 
 * @author Raphael Volz
 * 
 */
public class MultiAreaDownloadTask extends TrackableTask {

	Area[] areas;
	Area baseArea;
	String user;
	String passwd;
	String name;
	boolean loadContours;
	List<File> maps = new ArrayList<File>();
	List<File> pois = new ArrayList<File>();
	List<File> contours = new ArrayList<File>();

	/**
	 * Constructs a new download task with given parameters
	 * 
	 * @param progressMessage
	 *            displayed to the user while downloading
	 * @param user
	 *            nogago.com userName
	 * @param passwd
	 *            nogago.com passWd
	 * @param name
	 *            name for the file
	 */
	public MultiAreaDownloadTask(String progressMessage, String user,
			String passwd, String name, Area[] areas, Area baseArea, boolean loadContours) {
		super(progressMessage);
		this.user = user;
		this.passwd = passwd;
		this.areas = areas;
		this.name = name;
		this.baseArea = (baseArea==null?areas[0]:baseArea);
		this.loadContours = loadContours;
	}

	/* Separate Thread */
	@Override
	public Object doInBackground(Object... arg0) {
		try {
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			schemeRegistry.register(new Scheme("http", SSLSocketFactory.getSocketFactory(), 80));

			HttpParams params = new BasicHttpParams();
			SingleClientConnManager scm = new SingleClientConnManager(params, schemeRegistry);

			DefaultHttpClient client = new DefaultHttpClient(scm, params);
			Credentials defaultcreds = new UsernamePasswordCredentials(user, passwd);
			client.getCredentialsProvider().setCredentials(new AuthScope("download.nogago.com", 443, AuthScope.ANY_REALM), defaultcreds);
			publishProgress(0);
			
			for (int a = 0; a < areas.length && !isCancelled(); a++) {
				HttpGet get = new HttpGet(areas[a].getMapUrl());
				HttpResponse response = client.execute(get);
				
				if (!response.getStatusLine().toString().contains(Constants.HTTP_CODE_401+"")){
					File mapFile = new File((String) areas[a].getMapFilePath(name, baseArea));
					File contourFile = new File((String) areas[a].getContourFilePath(name, baseArea));
					maps.add(mapFile);
					contours.add(contourFile);
//					pois.add(poiFile);
					
					try {
						//Download map
						downloadResouce(response.getEntity(), mapFile, a*2);
/*
						//Download poi
						get = new HttpGet(areas[a].getPoiUrl());
						response = client.execute(get);
						downloadResouce(response.getEntity(), poiFile, a*2+1);
*/
						
						//Download poly if required
						if (loadContours == true) {
							get = new HttpGet(areas[a].getContourUrl());
							response = client.execute(get);
							downloadResouce(response.getEntity(), contourFile, a*2);
						}
						
					} catch (DownloadTaskException e) {
						return e;
					} 
				} else {
					client.getConnectionManager().shutdown();
					// HttpEntity is null
					return new DownloadTaskException(this, DownloadTaskException.CREDENTIALS_WRONG);
				}
			}
			client.getConnectionManager().shutdown();
			if (!isCancelled())
				return new Boolean(true);
			else {
				return new Boolean(false);
			}
		} catch (FileNotFoundException e1) {
			return new DownloadTaskException(this, DownloadTaskException.FILE_NOT_FOUND, e1);
		} catch (IOException e2) {
			return new DownloadTaskException(this, DownloadTaskException.IO_PROBLEM, e2);
		}
	}
	
	private void downloadResouce(HttpEntity entity, File resource, int a) throws IOException, DownloadTaskException{
		long i = 0;
		// Determine west east north
		// Create the file or overwrite
//		File tempFile = new File(Environment.getExternalStorageDirectory().toString() + Constants.TEMP_PATH + System.currentTimeMillis());
		File tempFile = new File(OsmandApplication.getSettings().extendOsmandPath(Constants.TEMP_PATH).toString() + "///" + System.currentTimeMillis());
		OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile.getAbsolutePath()));
		long len = entity.getContentLength();
		if (len < DownloadTask.getExternalAvailableSpaceInBytes()) {
			int baseProgress = (int)(a*100/areas.length);
			publishProgress(baseProgress);
			InputStream is = entity.getContent();
			byte[] buf = new byte[4096];
			int read;
			while ((read = is.read(buf)) != -1 && !isCancelled()) {
				i += 409600 ;
				publishProgress((int)(i/ len / areas.length ) + baseProgress);
				out.write(buf, 0, read);
			}
			// Finish up
			is.close();
			out.close();
		} else {
			out.close();
			throw new DownloadTaskException(this, DownloadTaskException.NOT_ENOUGH_SPACE);
		}
		try {
			if(!isCancelled()){ 
				if(tempFile.length() == len){
					FileUtils.copyFile(tempFile, resource);
				}else{
					throw new DownloadTaskException(this, DownloadTaskException.FAILED);
				}
			}
		} catch (IOException e) {
			throw new DownloadTaskException(this, DownloadTaskException.UNABLE_TO_COPY, e);
		}
		FileUtils.deleteQuietly(tempFile);
	}

	@Override
	public void cleanup() {
		for(File map: maps){
			FileUtils.deleteQuietly(map);
		}
		for(File poi: pois){
			FileUtils.deleteQuietly(poi);
		}
	}
}
