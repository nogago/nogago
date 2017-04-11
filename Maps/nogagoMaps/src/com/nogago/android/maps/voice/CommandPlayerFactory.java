package com.nogago.android.maps.voice;

import java.io.File;

import com.nogago.android.maps.R;
import com.nogago.android.maps.plus.OsmandApplication;
import com.nogago.android.maps.plus.ResourceManager;

import android.app.Activity;
import android.os.Build;

public class CommandPlayerFactory 
{
	public static CommandPlayer createCommandPlayer(String voiceProvider, OsmandApplication osmandApplication, Activity ctx)
		throws CommandPlayerException
	{
		if (voiceProvider != null){
			File parent = OsmandApplication.getSettings().extendOsmandPath(ResourceManager.VOICE_PATH);
			File voiceDir = new File(parent, voiceProvider);
			if(!voiceDir.exists()){
				throw new CommandPlayerException(ctx.getString(R.string.voice_data_unavailable));
			}
			if (MediaCommandPlayerImpl.isMyData(voiceDir)) {
				return new MediaCommandPlayerImpl(osmandApplication, voiceProvider);
			} else if (Integer.parseInt(Build.VERSION.SDK) >= 4) {
				if (TTSCommandPlayerImpl.isMyData(voiceDir)) {
					return new TTSCommandPlayerImpl(ctx, voiceProvider);
				}
			}
			throw new CommandPlayerException(ctx.getString(R.string.voice_data_not_supported));
		}
		return null;
	}
}
