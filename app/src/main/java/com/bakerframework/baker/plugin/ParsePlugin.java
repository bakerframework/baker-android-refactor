package com.bakerframework.baker.plugin;

import android.app.Activity;
import android.util.Log;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;
import com.bakerframework.baker.model.Issue;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import org.solovyev.android.checkout.Sku;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2013-2014. Francisco Contreras, Holland Salazar.
 * Copyright (c) 2015. Tobias Strebitzer, Francisco Contreras, Holland Salazar.
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * <p/>
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
 */
public class ParsePlugin implements BakerPlugin {

    public ParsePlugin() {

        // Application id, client key
        Parse.initialize(BakerApplication.getInstance(), BakerApplication.getInstance().getString(R.string.parse_application_id), BakerApplication.getInstance().getString(R.string.parse_client_key));
        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("ParsePlugin", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("ParsePlugin", "failed to subscribe for push", e);
                }
            }
        });
    }

    // Navigation Events

    @Override
    public void onSplashActivityCreated(Activity activity) {
        // Track app opened event
        ParseAnalytics.trackAppOpenedInBackground(activity.getIntent());
    }

    @Override
    public void onShelfActivityCreated(Activity activity) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("Activity", "Shelf");
        ParseAnalytics.trackEventInBackground("Navigate", dimensions);
    }

    @Override
    public void onIssueActivityCreated(Activity activity) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("Activity", "Issue");
        ParseAnalytics.trackEventInBackground("Navigate", dimensions);
    }

    // Shelf / Issue Events

    @Override
    public void onIssueDownloadClicked(Issue issue) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("Title", issue.getTitle());
        dimensions.put("Paid", issue.hasPrice() ? "yes" : "no");
        dimensions.put("Purchased", issue.isPurchased() ? "yes" : "no");
        ParseAnalytics.trackEventInBackground("Download_Issue_Clicked", dimensions);
    }

    @Override
    public void onIssueArchiveClicked(Issue issue) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("Title", issue.getTitle());
        dimensions.put("Paid", issue.hasPrice() ? "yes" : "no");
        dimensions.put("Purchased", issue.isPurchased() ? "yes" : "no");
        ParseAnalytics.trackEventInBackground("Archive_Issue_Clicked", dimensions);
    }

    @Override
    public void onIssueReadClicked(Issue issue) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("Title", issue.getTitle());
        dimensions.put("Paid", issue.hasPrice() ? "yes" : "no");
        dimensions.put("Purchased", issue.isPurchased() ? "yes" : "no");
        ParseAnalytics.trackEventInBackground("Read_Issue_Clicked", dimensions);
    }

    // Issue Navigation Events

    public void onIssuePageOpened(Issue issue, String pageTitle, int pageIndex) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("Issue Title", issue.getTitle());
        dimensions.put("Page Title", pageTitle);
        dimensions.put("Page Index", String.valueOf(pageIndex));
        ParseAnalytics.trackEventInBackground("View_Issue_Page", dimensions);
    }

    // Purchase Events

    @Override
    public void onIssuePurchaseClicked(Issue issue) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("Title", issue.getTitle());
        dimensions.put("Paid", issue.hasPrice() ? "yes" : "no");
        dimensions.put("Purchased", issue.isPurchased() ? "yes" : "no");
        dimensions.put("Price", issue.getPrice());
        ParseAnalytics.trackEventInBackground("Purchase_Issue_Clicked", dimensions);
    }

    @Override
    public void onSubscribeClicked(Sku subscription) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("Title", subscription.title);
        dimensions.put("Id", subscription.id);
        dimensions.put("Price", subscription.price);
        ParseAnalytics.trackEventInBackground("Subscribe_Clicked", dimensions);
    }

}
