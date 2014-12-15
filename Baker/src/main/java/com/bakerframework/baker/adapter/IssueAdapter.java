package com.bakerframework.baker.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bakerframework.baker.model.Issue;
import com.bakerframework.baker.model.Manifest;
import com.bakerframework.baker.views.IssueView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by tstrebitzer on 14/12/14.
 */
public class IssueAdapter extends ArrayAdapter {

    private Activity context;
    private List<Issue> issues = Collections.emptyList();
    private List<Issue> filteredIssues = Collections.emptyList();
    private String category = null;
    private Manifest manifest;
    private boolean filterChanged = false;

    private class IssueDateComparator implements Comparator<Issue> {
        @Override
        public int compare(Issue i1, Issue i2) {
            return i1.getDate().compareTo(i2.getDate());
        }
    }

    public IssueAdapter(Activity context, Manifest manifest) {
        super(context, 0);
        this.context = context;
        this.category = Manifest.ALL_CATEGORIES_STRING;
        this.manifest = manifest;
        updateIssues(false);
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
        return manifest.getCategories().indexOf(category);
    }

    public void updateIssues() {
        updateIssues(true);
    }

    public void updateIssues(boolean notify) {
        this.issues = manifest.getIssues();
        sortByDate();
        processFilters();
        filterChanged = true;
        if(notify) { notifyDataSetChanged(); }
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
        // Check if cached view exists, and if the cached view's issue name equals the issue at the current position in the dataset
        if(convertView != null && ((IssueView) convertView).getIssue().getName() == getItem(position).getName()) {
            // Don't recreate the view if it already exists
            // @TODO: update view if necessary
            return convertView;
        }else{
            // Create a new issue view
            Issue issue = filteredIssues.get(position);
            IssueView issueView = new IssueView(context, issue);
            issueView.init(context, null);
            return issueView;
        }
    }

    public void processFilters() {

        if(category == Manifest.ALL_CATEGORIES_STRING) {
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

    public Issue getItemByName(String name) {
        for(Issue issue : issues) {
            if(issue.getName() != null && issue.getName().equals(name)) {
                return issue;
            }
        }
        return null;
    }

}

