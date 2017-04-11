package com.nogago.android.task;

import android.os.AsyncTask;

public interface OnTaskCompleteListener {
	
	
    /** Notifies about task completeness */
    void onTaskComplete(AsyncTask task, Object result);

    /** Notifies about task completeness */
    void onTaskCancel(AsyncTask task);
    

    /** Notifies about task completeness */
    void onTaskError(AsyncTask task, Exception error);
}