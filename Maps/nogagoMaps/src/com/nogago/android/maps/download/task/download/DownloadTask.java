package com.nogago.android.maps.download.task.download;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
import com.nogago.android.maps.plus.OsmandApplication;
import com.nogago.android.maps.plus.ResourceManager;

import android.os.Environment;
import android.os.StatFs;

/**
 * Task to download from download.nogago.com via HTTPS authenticated
 * credentials.
 * 
 * @author Raphael Volz
 * 
 */
public class DownloadTask extends TrackableTask {

	String urltext;
	String user;
	String passwd;
	String filePath;
	int partSize;

	/**
	 * Constructs a new download task with given parameters
	 * 
	 * @param progressMessage
	 *            displayed to the user while downloading
	 * @param url
	 *            url that will be downloaded from nogago.com
	 * @param user
	 *            nogago.com userName
	 * @param passwd
	 *            nogago.com passWd
	 * @param filename
	 *            filename (full path) where to store the downloaded content
	 */
	public DownloadTask(String progressMessage, String url, String user, String passwd, String filename) {
		super(progressMessage);
		this.urltext = url;
		this.user = user;
		this.passwd = passwd;
		this.filePath = filename;
	}

	/* Separate Thread */
	@Override
	protected Object doInBackground(Object... args) {
		int a = (Integer)args[0];
//		File tempFile = new File(Environment.getExternalStorageDirectory().toString() + Constants.TEMP_PATH + System.currentTimeMillis());
		File tempFile = new File(OsmandApplication.getSettings().extendOsmandPath(Constants.TEMP_PATH).toString() + System.currentTimeMillis());
		try {
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			schemeRegistry.register(new Scheme("http", SSLSocketFactory.getSocketFactory(), 80));
			HttpParams params = new BasicHttpParams();
			SingleClientConnManager scm = new SingleClientConnManager(params,schemeRegistry);

			DefaultHttpClient client = new DefaultHttpClient(scm, params);
			Credentials defaultcreds = new UsernamePasswordCredentials(user, passwd);
			client.getCredentialsProvider().setCredentials(new AuthScope("download.nogago.com", 443, AuthScope.ANY_REALM), defaultcreds);
			
			int baseProgress = (int)((a-1)*100/partSize);
			publishProgress(baseProgress);
			
			HttpGet get = new HttpGet(urltext);
			HttpResponse response = client.execute(get);
			if (!response.getStatusLine().toString().contains(Constants.HTTP_CODE_401+"")) {
				HttpEntity entity = response.getEntity();
				// Create the file or overwrite
				OutputStream out = new BufferedOutputStream(
						new FileOutputStream(tempFile.getAbsolutePath()));
				long len = entity.getContentLength();
				if (len*2 < getExternalAvailableSpaceInBytes()) {
					long i = 0;
					InputStream is = entity.getContent();
					byte[] buf = new byte[4096];
					int read;
					while ((read = is.read(buf)) != -1 && !isCancelled()) {
						i += 409600; // such that progress is between 0 and 100
						publishProgress(((int) (i/len/partSize)) + baseProgress);
						out.write(buf, 0, read);
					}

					// Finish up
					is.close();
					out.close();
					client.getConnectionManager().shutdown();
				} else {
					out.close();
					client.getConnectionManager().shutdown();
					return new DownloadTaskException(this, DownloadTaskException.NOT_ENOUGH_SPACE);
				}
				if (!isCancelled()){
					try {
						FileUtils.copyFile(tempFile, new File(filePath));
					} catch (IOException e) {
						return new DownloadTaskException(this, DownloadTaskException.UNABLE_TO_COPY, e);
					}
					FileUtils.deleteQuietly(tempFile);
					return new Boolean(true);
				}else {
					FileUtils.deleteQuietly(tempFile);
					return new Boolean(false);
				}
			} else {
				// HttpEntity is null
				return new DownloadTaskException(this, DownloadTaskException.CREDENTIALS_WRONG);
			}
		} catch (FileNotFoundException e1) {
			// e1.printStackTrace();
			return new DownloadTaskException(this, DownloadTaskException.FILE_NOT_FOUND, e1);
		} catch (IOException e2) {
			// e2.printStackTrace();
			return new DownloadTaskException(this, DownloadTaskException.IO_PROBLEM, e2);
		}
	}

	/**
	 * @return Number of bytes available on external storage
	 */
	public static long getExternalAvailableSpaceInBytes() {
		long availableSpace = -1L;
		try {
//			StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
			StatFs stat = new StatFs(OsmandApplication.getSettings().extendOsmandPath(ResourceManager.APP_DIR).getPath());
			//stat.restat(Environment.getDataDirectory().getPath());
			availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return availableSpace;
	}
	
	public void setFilePath(String filePath){
		this.filePath = filePath;
	}

	public void setPartSize(int partSize) {
		this.partSize = partSize;
	}
	

	public void setUrl(String urltext) {
		this.urltext = urltext;
	}

}
