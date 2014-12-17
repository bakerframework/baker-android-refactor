package com.bakerframework.baker.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.GridView;

import com.bakerframework.baker.adapter.IssueAdapter;
import com.bakerframework.baker.model.Issue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tstrebitzer on 14/12/14.
 */
public class ShelfView extends GridView {

    public ShelfView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IssueCardView getViewByIssue(Issue issue) {
        int position = ((IssueAdapter) getAdapter()).getPosition(issue);
        return (IssueCardView) getChildAt(position);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        this.setNumColumns(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2);
        super.onConfigurationChanged(newConfig);
    }


}
