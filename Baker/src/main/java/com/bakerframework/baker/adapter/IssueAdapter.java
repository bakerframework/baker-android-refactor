package com.bakerframework.baker.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bakerframework.baker.model.Issue;
import com.bakerframework.baker.model.IssueCollection;
import com.bakerframework.baker.view.IssueCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Tobias Strebitzer <tobias.strebitzer@magloft.com> on 14/12/14.
 * http://www.magloft.com
 */
public class IssueAdapter extends ArrayAdapter {

    private Activity context;
    private List<Issue> issues = Collections.emptyList();
    private List<Issue> filteredIssues = Collections.emptyList();
    private String category = null;
    private IssueCollection issueCollection;
    private boolean filterChanged = false;

    private class IssueDateComparator implements Comparator<Issue> {
        @Override
        public int compare(Issue i1, Issue i2) {
            return i1.getDate().compareTo(i2.getDate());
        }
    }

    public IssueAdapter(Activity context, IssueCollection issueCollection) {
        super(context, 0);
        this.context = context;
        this.category = IssueCollection.ALL_CATEGORIES_STRING;
        this.issueCollection = issueCollection;
    }

    public void setCategory(String category) {
        this.category = category;
        processFilters();
        notifyDataSetChanged();
    }

    public String getCategory() {
        return category;
    }

    public int getCategoryIndex() {
        return issueCollection.getCategories().indexOf(category);
    }

    public void updateIssues() {
        updateIssues(true);
    }

    public void updateIssues(boolean notify) {
        this.issues = issueCollection.getIssues();
        sortByDate();
        processFilters();
        filterChanged = true;
        if(notify) { notifyDataSetChanged(); }
    }

    public void processFilters() {

        if(category == IssueCollection.ALL_CATEGORIES_STRING) {
            // Reset filtered issues
            filteredIssues = new ArrayList<Issue>(issues);
        }else{
            // Process category filter
            filteredIssues = new ArrayList<Issue>();
            for(Issue issue : issues) {
                if(issue.isInCategory(category)) {
                    filteredIssues.add(issue);
                }
            }
        }

    }

    public void sortByDate() {
        if(issues != null && issues.size() > 0) {
            Collections.sort(issues, Collections.reverseOrder(new IssueDateComparator()));
        }
    }

    @Override
    public int getCount() {
        return filteredIssues.size();
    }

    // getItem(int) in Adapter returns Object but we can override
    // it to Issue thanks to Java return type covariance
    @Override
    public Issue getItem(int i) {
        return filteredIssues.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        // Check if a convertView exists
        if(convertView != null) {

            // Receive issue
            Issue issue = ((IssueCardView) convertView).getIssue();

            // Check if the view exists at the current location
            if(issue.getName() == getItem(position).getName()) {
                ((IssueCardView) convertView).redraw();
                return convertView;
            }

        }

        // If not, (re-)create the view and store it in cache
        Issue issue = getItem(position);
        IssueCardView issueCardView = new IssueCardView(context, issue);
        issueCardView.init(context, null);

        // Return view
        return issueCardView;

    }

}

