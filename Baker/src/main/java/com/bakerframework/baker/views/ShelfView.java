package com.bakerframework.baker.views;

import android.content.Context;
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

    public IssueView getViewByIssue(Issue issue) {
        int position = ((IssueAdapter) getAdapter()).getPosition(issue);
        return (IssueView) getChildAt(position);
    }


    public List<IssueView> getDownloadingViews() {
        List<IssueView> views = new ArrayList<IssueView>();
        for (int i = 0; i < getChildCount(); i++) {
            IssueView issueView = (IssueView) getChildAt(i);
            if(issueView.isDownloading()) {
                views.add(issueView);
            }
        }
        return views;
    }
}
