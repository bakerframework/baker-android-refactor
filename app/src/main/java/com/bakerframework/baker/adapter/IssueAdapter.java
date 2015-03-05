/**
 * Copyright (c) 2013-2014. Francisco Contreras, Holland Salazar.
 * Copyright (c) 2015. Tobias Strebitzer, Francisco Contreras, Holland Salazar.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * Neither the name of the Baker Framework nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
package com.bakerframework.baker.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bakerframework.baker.model.Issue;
import com.bakerframework.baker.model.IssueCollection;
import com.bakerframework.baker.model.RemoteIssueCollection;
import com.bakerframework.baker.view.IssueCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class IssueAdapter extends ArrayAdapter {

    private final Activity context;
    private List<Issue> issues = Collections.emptyList();
    private List<Issue> filteredIssues = Collections.emptyList();
    private String category = null;
    private final IssueCollection issueCollection;
    private final HashMap<Issue,IssueCardView> issueCardViewCache = new HashMap<>();

    private class IssueDateComparator implements Comparator<Issue> {
        @Override
        public int compare(Issue i1, Issue i2) {
            if(i1.getObjDate() != null) {
                return i1.getObjDate().compareTo(i2.getObjDate());
            }else{
                return 0;
            }
        }
    }

    public IssueAdapter(Activity context, IssueCollection issueCollection) {
        super(context, 0);
        this.context = context;
        this.category = RemoteIssueCollection.ALL_CATEGORIES_STRING;
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
        if(issueCollection.getCategories() == null) {
            return 0;
        }else{
            return issueCollection.getCategories().indexOf(category);
        }
    }

    public void updateIssues() {
        updateIssues(true);
    }

    public void updateIssues(boolean notify) {
        this.issues = issueCollection.getIssues();
        sortByDate();
        processFilters();
        if(notify) { notifyDataSetChanged(); }
    }

    public void processFilters() {

        if(category == RemoteIssueCollection.ALL_CATEGORIES_STRING) {
            // Reset filtered issues
            filteredIssues = new ArrayList<>(issues);
        }else{
            // Process category filter
            filteredIssues = new ArrayList<>();
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
                // ((IssueCardView) convertView).redraw();
                return convertView;
            }
        }

        // If not, (re-)create the view and store it in cache
        Issue issue = getItem(position);

        if(issueCardViewCache.containsKey(issue)) {
            return issueCardViewCache.get(issue);
        }else{
            IssueCardView issueCardView = new IssueCardView(context, issue);
            issueCardView.init(context);
            issueCardViewCache.put(issue, issueCardView);
            return issueCardView;
        }

    }

}

