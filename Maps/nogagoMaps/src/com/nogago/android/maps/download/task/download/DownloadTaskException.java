package com.nogago.android.maps.download.task.download;



import com.nogago.android.maps.download.task.TrackableTask;
import com.nogago.android.maps.download.task.TrackableTaskException;


public class DownloadTaskException extends TrackableTaskException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final byte UNABLE_TO_CONNECT = 0x00;
	public static final byte FILE_NOT_FOUND = 0x01;
	public static final byte CREDENTIALS_WRONG = 0x02;
	public static final byte IO_PROBLEM = 0x03;
	public static final byte NOT_ENOUGH_SPACE = 0x04;
	public static final byte UNABLE_TO_COPY = 0x05;
	public static final byte FAILED = 0x06;

	public DownloadTaskException(TrackableTask task, byte id) {
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
	public DownloadTaskException(TrackableTask task, byte id, Throwable t) {
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
			return "Unable to Connect";
		default:
			return super.getMessageById(id);
		}
	}
}
