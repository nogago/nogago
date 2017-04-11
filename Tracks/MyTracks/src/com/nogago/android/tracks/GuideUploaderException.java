package com.nogago.android.tracks;

public class GuideUploaderException extends Exception {

	private static final long serialVersionUID = 302211889279167146L;

	public static final byte WRONG_CREDENTIALS = 0x00;

	public static final byte INVALID_GUIDE_FORMAT = 0x01;
	
	private byte id;
	
	public GuideUploaderException(byte id) {
		super();
		this.id = id;
	}
	
	public byte getId() {
		return this.id;
	}
}
