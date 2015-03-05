package com.bakerframework.baker.plugin;

import android.app.Activity;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;
import com.bakerframework.baker.model.Issue;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.ParseAnalytics;

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
public class GoogleAnalyticsPlugin implements BakerPlugin {
    final Tracker tracker;

    public GoogleAnalyticsPlugin() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(BakerApplication.getInstance());
        tracker = analytics.newTracker(BakerApplication.getInstance().getString(R.string.google_analytics_tracking_id));
    }

    // Navigation Events

    @Override
    public void onSplashActivityCreated(Activity activity) {
        sendScreen("Splash Screen");
        sendEvent("Magazine", "Magazine Launched", "Magazine Launched");
    }

    @Override
    public void onShelfActivityCreated(Activity activity) {
        sendScreen("Shelf Screen");
    }

    @Override
    public void onIssueActivityCreated(Activity activity) {
        sendScreen("Issue Screen");
    }

    // Shelf / Issue Events

    @Override
    public void onIssueDownloadClicked(Issue issue) {
        sendEvent("Issue", "Download Issue Clicked", issue.getTitle());
    }

    @Override
    public void onIssueArchiveClicked(Issue issue) {
        sendEvent("Issue", "Archive Issue Clicked", issue.getTitle());
    }

    @Override
    public void onIssueReadClicked(Issue issue) {
        sendEvent("Issue", "Read Issue Clicked", issue.getTitle());
    }

    // Issue Navigation Events

    public void onIssuePageOpened(Issue issue, String pageTitle, int pageIndex) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("Issue Title", issue.getTitle());
        dimensions.put("Page Title", pageTitle);
        dimensions.put("Page Index", String.valueOf(pageIndex));
        ParseAnalytics.trackEventInBackground("View_Issue_Page", dimensions);
        sendEvent("Issue", "Page Views", issue.getTitle() + pageTitle);
    }

    // Purchase Events

    @Override
    public void onIssuePurchaseClicked(Issue issue) {
        sendEvent("Purchase Actions", "Purchase Button Clicked", issue.getTitle());
    }

    @Override
    public void onSubscribeClicked(Sku subscription) {
        sendEvent("Purchase Actions", "Subscribe Button Clicked", subscription.title);
    }

    // Private methods

    private void sendScreen(String screenName) {
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.AppViewBuilder().build());
    }

    private void sendEvent(String category, String action, String label) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }

}
