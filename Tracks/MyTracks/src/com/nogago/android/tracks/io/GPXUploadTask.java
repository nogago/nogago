package com.nogago.android.tracks.io;

import com.google.android.apps.mytracks.content.MyTracksProviderUtils;
import com.google.android.apps.mytracks.io.file.TrackWriter;
import com.google.android.apps.mytracks.io.file.TrackWriterFactory;
import com.google.android.apps.mytracks.io.file.TrackWriterFactory.TrackFileFormat;
import com.nogago.android.task.TrackableTask;
import com.nogago.android.tracks.Constants;

import android.content.Context;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

/**
 * Task to upload a GPX file to www.nogago.com via HTTPS authenticated
 * credentials.
 * 
 * @author Raphael Volz
 */
public class GPXUploadTask extends TrackableTask {
  
  private TrackWriter trackWriter;
  private final Context context;
  private final MyTracksProviderUtils myTracksProviderUtils;
  
  private int messageId;

  private String savedPath;
  

  String user;
  String passwd;
  long trackID;

  /**
   * Constructs a new download task with given parameters
   * 
   * @param progressMessage displayed to the user while downloading
   * @param user nogago.com userName
   * @param passwd nogago.com passWd
   * @param name name for the file
   */
  public GPXUploadTask(Context context, String progressMessage, String user, String passwd, long trackID ) {
    super(progressMessage);
    myTracksProviderUtils = MyTracksProviderUtils.Factory.get(context);
    this.context = context;
    this.user = user;
    this.passwd = passwd;
    this.trackID = trackID;
   
  }

 
  /* Old Version
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
      client.getCredentialsProvider().setCredentials(
          new AuthScope("www.nogago.com", 443, AuthScope.ANY_REALM), defaultcreds);
      publishProgress(0);
      HttpPost post = new HttpPost(Constants.TRACKS_UPLOAD_URL);
      
      // Getting Track Content
      int responseCode = 0;
      trackWriter = TrackWriterFactory.newWriter(
          context, myTracksProviderUtils, trackID, TrackFileFormat.GPX);
      StringEntity entity = new StringEntity(trackWriter.writeTrackAsString());
      
      // Getting Track Name
      Track track = myTracksProviderUtils.getTrack(trackID);
            
//      params.setParameter("username", user);
//      params.setParameter("password", passwd);
      params.setParameter("trackName", track.getName().replace(":",
          "_") );
      post.setEntity( entity );
      HttpResponse response = client.execute(post);
      String status = response.getStatusLine().toString();
      if(response.getStatusLine().toString().contains("400")) {
        // AN error occured
        return new UploadTaskException(this, UploadTaskException.INVALID_GPX);
      }      
      if(response.getStatusLine().toString().contains("401")) {
        // AN error occured
        return new UploadTaskException(this, UploadTaskException.CREDENTIALS_WRONG);
      }
      client.getConnectionManager().shutdown();

    } catch (IOException e3) {
      return new UploadTaskException(this, UploadTaskException.IO_PROBLEM);
    }
    return new String("OK");
  }
*/
  
  
  /* Separate Thread */
  @Override
  public Object doInBackground(Object... arg0) {
    

    trackWriter = TrackWriterFactory.newWriter(
        context, myTracksProviderUtils, trackID, TrackFileFormat.GPX);
    String s = trackWriter.writeTrackAsString();
    String trackName = myTracksProviderUtils.getTrack(trackID).getName().replace(":",
        "_") ;
    
    try {
        // create the POST data
        StringBuffer data = new StringBuffer();
        data.append("username=").append(user);
        data.append('&').append("password=").append(passwd);
        data.append('&').append("trackName=").append(trackName);
        data.append('&').append("content=");
        byte[] dataHeader = data.toString().getBytes("UTF-8");
        byte[] dataGPX = s.getBytes("UTF-8");
        byte[] postBytes = new byte[dataHeader.length + dataGPX.length];
        System.arraycopy(dataHeader, 0, postBytes, 0, dataHeader.length);
        System.arraycopy(dataGPX, 0, postBytes, dataHeader.length,
                dataGPX.length);

        // prepare and execute the POST
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(Constants.TRACKS_UPLOAD_URL);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(new ByteArrayEntity(postBytes));
        HttpResponse response = client
                .execute(post, new BasicHttpContext());

        // read the status
        StatusLine statusLine = response.getStatusLine();
        int status = statusLine.getStatusCode();

        // if the
        switch (status) {
        case 400:
          return new UploadTaskException(this, UploadTaskException.INVALID_GPX);
        case 401:
          return new UploadTaskException(this, UploadTaskException.CREDENTIALS_WRONG);
        }
        ;

    } catch (IOException e3) {
      return new UploadTaskException(this, UploadTaskException.IO_PROBLEM);
    } finally {
        publishProgress(100);
    }
    return new String("OK");
  }



}
