package com.nogago.android.maps.download.task.update;

import com.nogago.android.maps.download.task.TrackableTask;
import com.nogago.android.maps.download.task.TrackableTaskException;

public class UpdateMapException  extends TrackableTaskException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final byte NOT_ENOUGH_SPACE = 0x01;

	public UpdateMapException(TrackableTask task, byte id) {
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
	public UpdateMapException(TrackableTask task, byte id, Throwable t) {
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
		return super.getMessageById(id);
	}
}
