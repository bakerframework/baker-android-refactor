package com.bakerframework.baker.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.widget.AdapterView;

import com.bakerframework.baker.R;

/**
 * Created by tstrebitzer on 15/12/14.
 */
public class SwipeRefreshScrollLayout extends SwipeRefreshLayout {

    AdapterView adapterView;
    int adapterViewId;

    public SwipeRefreshScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SwipeRefreshScrollLayout, 0, 0);
        adapterViewId = styledAttributes.getResourceId(R.styleable.SwipeRefreshScrollLayout_adapter_view, -1);
        styledAttributes.recycle ();

        // Set refresh indicator colors
        setColorSchemeColors(Color.RED, Color.BLUE, Color.RED, Color.BLUE);

    }

    @Override
    protected void onFinishInflate () {
        super.onFinishInflate ();
        adapterView = (AdapterView) findViewById (adapterViewId);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean canChildScrollUp() {
        return adapterView.canScrollVertically (-1);
    }

}
