package com.sw.wikiimagesearch;

import android.content.Context;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

/**
 * Provides custom implementation of network image view.
 * @author  Swapnil Udar
 */
public class CustomNetworkImageView extends NetworkImageView {

    public CustomNetworkImageView(Context context) {
        super(context);
        setDefaultImageResId(R.mipmap.loading);
        setErrorImageResId(R.mipmap.error_while_loading);
    }

    public CustomNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefaultImageResId(R.mipmap.loading);
        setErrorImageResId(R.mipmap.error_while_loading);
    }

    public CustomNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDefaultImageResId(R.mipmap.loading);
        setErrorImageResId(R.mipmap.error_while_loading);
    }
}
