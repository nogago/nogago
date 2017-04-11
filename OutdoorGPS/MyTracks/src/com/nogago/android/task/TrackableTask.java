package com.nogago.android.task;

import android.os.AsyncTask;

public abstract class TrackableTask extends AsyncTask<Object, Integer, Object> {

	private Object mResult;
	protected String mProgressMessage;
	private IProgressTracker mProgressTracker;

	public TrackableTask(String progressMessage) {
		// Initialise initial pre-execute message
		mProgressMessage = progressMessage;
	}

	public void setProgressTracker(IProgressTracker progressTracker) {
		// Attach to progress tracker
		
		mProgressTracker = progressTracker;
		// Initialise progress tracker with current task state
		if (mProgressTracker != null) {
			mProgressTracker.setProgressMessage(mProgressMessage);
			if (mResult != null) {
				mProgressTracker.onComplete(mResult);
			}
		}
	}

	/** Called from UI Thread when user hits back button. Working Thread should always check whether isCancelled() is true*/
	@Override
	protected void onCancelled() {
		// Detach from progress tracker
		mProgressTracker = null;
	}

	protected void onProgressUpdate(Integer... values) {
		// Update progress message
		Integer value = values[0];
		// And send it to progress tracker
		if (mProgressTracker != null) {
			mProgressTracker.onProgress(value.intValue());
		}
	}

	@Override
	protected void onPostExecute(Object result) {
		// Update result
		mResult = result;
		// And send it to progress tracker
		if (mProgressTracker != null) {
			mProgressTracker.onComplete(result);
		}
		// Detach from progress tracker
		mProgressTracker = null;
	}

	public void cleanup(){
		//By default nothing to do
	}
}