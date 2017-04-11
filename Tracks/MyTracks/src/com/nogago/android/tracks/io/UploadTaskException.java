package com.nogago.android.tracks.io;

import com.nogago.android.task.TrackableTask;
import com.nogago.android.task.TrackableTaskException;

public class UploadTaskException extends TrackableTaskException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static final byte UNABLE_TO_CONNECT = 0x00;
    public static final byte INVALID_GPX = 0x01;
    public static final byte CREDENTIALS_WRONG = 0x02;
    public static final byte IO_PROBLEM = 0x03;

    public UploadTaskException(TrackableTask task, byte id) {
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
    public UploadTaskException(TrackableTask task, byte id, Throwable t) {
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
        case INVALID_GPX:
          return "Invalid GPX File";
        case CREDENTIALS_WRONG:
          return "Credentials Wrong";
        case IO_PROBLEM:
          return "IO Problem";
        default:
            return super.getMessageById(id);
        }
    }
}
