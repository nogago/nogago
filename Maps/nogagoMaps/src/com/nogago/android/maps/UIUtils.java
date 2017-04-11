package com.nogago.android.maps;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

public class UIUtils {
	
	public static void showAlertDialog(Context context, String title, String message, OnClickListener listener){
		Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
	    builder.setIcon(android.R.drawable.ic_dialog_info);
	    builder.setMessage(message);
	    builder.setNeutralButton(R.string.button_ok, listener);
	    builder.show();
	}

}
