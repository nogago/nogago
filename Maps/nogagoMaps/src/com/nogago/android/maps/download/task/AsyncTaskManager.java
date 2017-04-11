package com.nogago.android.maps.download.task;

import com.nogago.android.maps.R;
import com.nogago.android.maps.activities.MapManagerActivity;
import com.nogago.android.maps.download.task.download.DeleteMapTask;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Utility Manager handling ProgressDialog display for arbitrary long running
 * asynchronous tasks that can be set via setupTask and retained via retainTask
 */
public final class AsyncTaskManager implements IProgressTracker,
		OnCancelListener {

	private final OnTaskCompleteListener mTaskCompleteListener;
	private final ProgressDialog mProgressDialog;
	private TrackableTask mAsyncTask;
	private String message;
	private boolean loadContours;

	/**
	 * Constructor requires context of calling Activity for UI work and provides
	 * callback to OnTaskCompleteListener and a message displayed to the user
	 * while waiting for Task to complete
	 */
	public AsyncTaskManager(Context context,
			OnTaskCompleteListener taskCompleteListener, String msg,
			boolean countable, boolean loadContours) {
		this.message = msg;
		this.loadContours = loadContours;
		// Save reference to complete listener (activity)
		mTaskCompleteListener = taskCompleteListener;
		// Setup progress dialog
		mProgressDialog = new ProgressDialog(context);
		if (message != null && loadContours == false)
			mProgressDialog.setMessage(message);
		if (message != null && loadContours == true) {
			mProgressDialog.setMessage("Lade Höhenlinien");
		}
		if (countable)
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		else
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setProgress(0);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setButton(context.getString(R.string.button_cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mProgressDialog.isShowing())
							mProgressDialog.dismiss();
						if (mAsyncTask == null) {
							return;
						}
						mAsyncTask.cancel(true);
						// Notify activity about completion
						mTaskCompleteListener.onTaskComplete(mAsyncTask);
						// Reset task
						mAsyncTask = null;
					}
				});
		mProgressDialog.setOnCancelListener(this);
	}

	/** sets up the task to run and watch progress for */
	public void setupTask(TrackableTask asyncTask) {
		// Keep task
		mAsyncTask = asyncTask;
		// Wire task to tracker (this)
		mAsyncTask.setProgressTracker(this);
		// Start task
		mAsyncTask.execute();
	}

	@Override
	public void setProgressMessage(String msg) {
		this.message = msg;
	}

	/** Call back from task to update the progress */
	@Override
	public void onProgress(int percentage) {
		// Show dialog if it wasn't shown yet or was removed on configuration
		// (rotation) change
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}
		// Show current message in progress dialog
		mProgressDialog.setProgress(percentage);
		mProgressDialog.setMessage(message);
	}

	/** Call back to notify of cancellation */

	@Override
	public void onCancel(DialogInterface dialog) {

		// Cancel task
		if (mProgressDialog.isShowing())
			mProgressDialog.dismiss();
		if (mAsyncTask == null) {
			return;
		}
		mAsyncTask.cancel(true);
		// Notify activity about completion
		mTaskCompleteListener.onTaskComplete(mAsyncTask);
		// Reset task
		mAsyncTask = null;
	}

	/** Call back to notify of completion */
	@Override
	public void onComplete() {
		// Close progress dialog
		try {
		if (mProgressDialog != null && mProgressDialog.isShowing())
				mProgressDialog.dismiss();
		} catch (IllegalArgumentException e) {
			Log.e("AsyncTaskManager", "Dialog no longer visible", e);
		}
		// Notify activity about completion
		mTaskCompleteListener.onTaskComplete(mAsyncTask);
		// Reset task
		mAsyncTask = null;
	}

	public Object retainTask() {
		// Detach task from tracker (this) before retain
		if (mAsyncTask != null) {
			mAsyncTask.setProgressTracker(null);
		}
		// Retain task
		return mAsyncTask;
	}

	public void handleRetainedTask(Object instance) {
		// Restore retained task and attach it to tracker (this)
		if (instance instanceof TrackableTask) {
			mAsyncTask = (TrackableTask) instance;
			mAsyncTask.setProgressTracker(this);
		}
	}

	public boolean isWorking() {
		// Track current status
		return mAsyncTask != null;
	}
}