package com.nogago.android.maps.download.task.readareas;

import com.nogago.android.maps.download.task.TrackableTask;
import com.nogago.android.maps.download.task.TrackableTaskException;

public class ReadAreasTaskException extends TrackableTaskException {
	static final byte NUMBER_FORMAT_EXCEPTION = 0x10;
	public static final byte FILE_NOT_FOUND = 0x11;
	public static final byte IO_PROBLEM = 0x12;
	public static final byte NOT_INITIALIZED= 0x13;
	
	public ReadAreasTaskException(TrackableTask task, byte id, Throwable t) {
		super(task, id, t);
	}
	public ReadAreasTaskException(TrackableTask task, byte id) {
		super(task, id);
	}

}
