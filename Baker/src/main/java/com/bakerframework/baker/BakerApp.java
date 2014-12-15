/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bakerframework.baker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class BakerApp extends Application implements AnalyticsEvents {

    private static Context context;
    private static SharedPreferences preferences;

    public enum TrackerName {
        GLOBAL_TRACKER
    }

    private HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public BakerApp() {
        super();
    }

    public void onCreate(){
        super.onCreate();
        BakerApp.context = getApplicationContext();
        BakerApp.preferences = context.getSharedPreferences("baker.app", 0);
    }

    public static Context getAppContext() {
        return BakerApp.context;
    }

    // App State
    public static boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return (ni != null);
    }

    // System

    public static int getVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    // Preferences

    public static SharedPreferences getPreferences() {
        return BakerApp.preferences;
    }

    public static int getPreferenceInt(String field) {
        return getPreferenceInt(field, -1);
    }

    public static int getPreferenceInt(String field, int defaultValue) {
        return preferences.getInt(field, defaultValue);
    }

    public static String getPreferenceString(String field) {
        return getPreferenceString(field, null);
    }

    public static String getPreferenceString(String field, String defaultValue) {
        return preferences.getString(field, defaultValue);
    }

    public static Boolean getPreferenceBoolean(String field) {
        return getPreferenceBoolean(field, false);
    }

    public static Boolean getPreferenceBoolean(String field, boolean defaultValue) {
        return preferences.getBoolean(field, defaultValue);
    }

    public static void setPreferenceInt(String field, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(field, value);
        editor.commit();
    }

    public static void setPreferenceString(String field, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(field, value);
        editor.commit();
    }

    public static void setPreferenceBoolean(String field, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(field, value);
        editor.commit();
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

    /**
     * We return our only configured tracker.
     *
     * @param trackerId the name of tracker, in case others are added.
     * @return Tracker the Tracker.
     */
    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker tracker = (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : null;
            mTrackers.put(trackerId, tracker);

        }
        return mTrackers.get(trackerId);
    }
}
