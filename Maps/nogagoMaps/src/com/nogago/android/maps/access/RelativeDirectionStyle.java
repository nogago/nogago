package com.nogago.android.maps.access;

import com.nogago.android.maps.R;

import android.content.Context;

public enum RelativeDirectionStyle {

    SIDEWISE(R.string.direction_style_sidewise),
    CLOCKWISE(R.string.direction_style_clockwise);

    private final int key;

    RelativeDirectionStyle(int key) {
        this.key = key;
    }

    public static String toHumanString(RelativeDirectionStyle style, Context ctx) {
        return ctx.getResources().getString(style.key);
    }

}
