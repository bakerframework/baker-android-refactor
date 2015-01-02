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
package com.bakerframework.baker;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.bakerframework.baker.model.IssueCollection;
import com.bakerframework.baker.play.ApiPurchaseVerifier;
import com.bakerframework.baker.play.LicenceManager;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Cache;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Products;
import org.solovyev.android.checkout.PurchaseVerifier;

import static java.util.Arrays.asList;
import static org.solovyev.android.checkout.ProductTypes.IN_APP;
import static org.solovyev.android.checkout.ProductTypes.SUBSCRIPTION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BakerApplication extends Application implements AnalyticsEvents {

    // Instance configuration
    private static BakerApplication sInstance;

    // Application mode (online/offline)
    public static final int APPLICATION_MODE_OFFLINE = 0;
    public static final int APPLICATION_MODE_ONLINE = 1;

    // Billing support
    private final Billing billing = new Billing(this, new Billing.DefaultConfiguration() {

        @Override
        public PurchaseVerifier getPurchaseVerifier() {
            return new ApiPurchaseVerifier();
        }

        @Override
        public String getPublicKey() {
            return "remote";
        }

        @Override
        public Cache getCache() {
            return Billing.newCache();
        }

    });
    private Checkout checkout;

    // Instance variables
    private SharedPreferences preferences;
    private IssueCollection issueCollection;
    private LicenceManager licenceManager;
    private int applicationMode = 1;

    public enum TrackerName {
        GLOBAL_TRACKER
    }

    private HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    public BakerApplication() {
        super();
    }

    @Override
    public void onCreate(){
        super.onCreate();
        sInstance = this;
        sInstance.initializeInstance();
    }

    protected void initializeInstance() {
        preferences = getSharedPreferences("baker.app", 0);
        issueCollection = new IssueCollection();
        licenceManager = new LicenceManager();
    }

    public static BakerApplication getInstance() {
        return sInstance;
    }

    // Getters

    public IssueCollection getIssueCollection() {
        return issueCollection;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public LicenceManager getLicenceManager() {
        return licenceManager;
    }

    public Billing getBilling() {
        return billing;
    }

    public void initializeCheckout(List<String> productIds) {
        List<String> subscriptionIds = new ArrayList<>();
        subscriptionIds.add(getString(R.string.subscription_product_id));
        checkout = Checkout.forApplication(billing, Products.create().add(IN_APP, productIds).add(SUBSCRIPTION, subscriptionIds));
    }

    public Checkout getCheckout() {
        return checkout;
    }

    public int getApplicationMode() {
        return applicationMode;
    }
    public void setApplicationMode(int applicationMode) {
        this.applicationMode = applicationMode;
    }

    // Helper methods

    public boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 9000).show();
            }
            return false;
        }
        return true;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) sInstance.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return (ni != null);
    }

    public int getVersion() {
        try {
            PackageInfo packageInfo = sInstance.getPackageManager().getPackageInfo(sInstance.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    // Preferences

    public int getPreferenceInt(String field) {
        return getPreferenceInt(field, -1);
    }

    public int getPreferenceInt(String field, int defaultValue) {
        return preferences.getInt(field, defaultValue);
    }

    public String getPreferenceString(String field) {
        return getPreferenceString(field, null);
    }

    public String getPreferenceString(String field, String defaultValue) {
        return preferences.getString(field, defaultValue);
    }

    public Boolean getPreferenceBoolean(String field) {
        return getPreferenceBoolean(field, false);
    }

    public Boolean getPreferenceBoolean(String field, boolean defaultValue) {
        return preferences.getBoolean(field, defaultValue);
    }

    public void setPreferenceInt(String field, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(field, value);
        editor.apply();
    }

    public void setPreferenceString(String field, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(field, value);
        editor.apply();
    }

    public void setPreferenceBoolean(String field, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(field, value);
        editor.apply();
    }

    // Event handling

    @Override
    public void sendEvent(String category, String action, String label) {
        Tracker tracker = this.getTracker(TrackerName.GLOBAL_TRACKER);

        Log.d(this.getClass().getName(), "Sending event to Google Analytics with Category: "
                + category
                + ", Action: " + action
                + ", Label: " + label);

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }

    @Override
    public void sendTimingEvent(String category, long value, String name, String label) {
        Tracker tracker = this.getTracker(TrackerName.GLOBAL_TRACKER);

        Log.d(this.getClass().getName(), "Sending user timing event to Google Analytics with Category: "
                + category
                + ", Value: " + value
                + ", Name: " + name
                + ", Label: " + label);

        // Build and send timing.
        tracker.send(new HitBuilders.TimingBuilder()
                .setCategory(category)
                .setValue(value)
                .setVariable(name)
                .setLabel(label)
                .build());
    }

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker tracker = (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker) : null;
            mTrackers.put(trackerId, tracker);
        }
        return mTrackers.get(trackerId);
    }

}
