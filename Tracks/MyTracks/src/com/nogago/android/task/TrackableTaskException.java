package com.nogago.android.task;

import java.util.concurrent.ExecutionException;



/**
 * An exception to return in doInBackground() of a AsyncTask. It takes an id as an argument
 * to later identify the reason for the exception.
 * 
 * @author Andre
 *
 */
public abstract class TrackableTaskException extends ExecutionException {

	/**
	 * the id for an unknown error
	 */
	public static final byte UNKNOWN = Byte.MIN_VALUE;
	
	/**
	 * the message for an unknown error
	 */
	public static final String MESSAGE_UNKNOWN = "unknown error";
	
	/**
	 * the AsyncTask
	 */
	private final TrackableTask task;

	/**
	 * the id
	 */
	private final byte id;

	/**
	 * Returns true if the given result is not an exception, otherwise calls onException() on the given listener and returns false
	 * @param task the task
	 * @param listener the listener
	 * @param result the result
	 * @return true if the given result is not an exception otherwise false

	public static boolean handle(TrackableTask task, Object result) {
		if (result instanceof TrackableTaskException) {
			TrackableTaskException exception = (TrackableTaskException) result;
			task.onException(task, exception);
			return true;
		} else {
			return false;
		}
	}
	 */
	
	/**
	 * Creates a new AsyncTaskException instance
	 * 
	 * @param task the AsyncTask associated
	 * @param id the id 
	 */
	public TrackableTaskException(TrackableTask task, byte id) {
		super();

		this.task = task;
		this.id = id;
	}
	
	/**
	 * Creates a new AsyncTaskException instance
	 * 
	 * @param task the AsyncTask associated
	 * @param id the id 
	 * @param t the cause
	 */
	public TrackableTaskException(TrackableTask task, byte id, Throwable t) {
		super(t);

		this.task = task;
		this.id = id;
	}

	/**
	 * Returns the message for the given id
	 * 
	 * @param id
	 *            the id
	 * @return the message
	 */
	protected String getMessageById(byte id) {
		return MESSAGE_UNKNOWN;
	}

	/**
	 * Get the id
	 * 
	 * @return the id
	 */
	public byte getId() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#toString()
	 */
	public String toString() {
		return super.toString() + ":" + getMessageById(this.id);
	}
}
