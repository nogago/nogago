package com.nogago.android.maps.download.task.findcity;



import com.nogago.android.maps.download.task.TrackableTask;
import com.nogago.android.maps.download.task.TrackableTaskException;


public class FindCityTaskException extends TrackableTaskException {
	private static final long serialVersionUID = -1098732122843829948L;

	public static final byte UNABLE_TO_CONNECT = 0x00;

	public FindCityTaskException(TrackableTask task, byte id) {
		super(task, id);
	}

	/**
	 * Creates a new {@link InitializerTaskException} instance
	 * 
	 * @param task
	 *            the task
	 * @param id
	 *            the id
	 * @param t
	 *            the cause throwable
	 */
	public FindCityTaskException(TrackableTask task, byte id, Throwable t) {
		super(task, id, t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nogago.android.common.asynctask.AsyncTaskException#getMessageById
	 * (byte)
	 */
	protected String getMessageById(byte id) {
		switch (id) {
		case UNABLE_TO_CONNECT:
			return "unable to connect";
		default:
			return super.getMessageById(id);
		}
	}
}
