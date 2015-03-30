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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bakerframework.baker.handler.PluginManager;
import com.bakerframework.baker.model.IssueCollection;
import com.bakerframework.baker.model.LocalIssueCollection;
import com.bakerframework.baker.model.RemoteIssueCollection;
import com.bakerframework.baker.play.ApiPurchaseVerifier;
import com.bakerframework.baker.play.LicenceManager;
import com.bakerframework.baker.settings.Configuration;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.log.CustomLogger;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Cache;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Products;
import org.solovyev.android.checkout.PurchaseVerifier;
import org.xwalk.core.XWalkPreferences;

import static org.solovyev.android.checkout.ProductTypes.IN_APP;
import static org.solovyev.android.checkout.ProductTypes.SUBSCRIPTION;

public class BakerApplication extends Application {
    private static BakerApplication instance;
    private JobManager jobManager;
    private PluginManager pluginManager;

    public BakerApplication() {
        instance = this;
    }

    // Application mode (online/offline)
    public static final int APPLICATION_MODE_OFFLINE = 0;
    public static final int APPLICATION_MODE_ONLINE = 1;

    // Billing support
    private final Billing billing = new Billing(this, new Billing.DefaultConfiguration() {

        @NonNull
        @Override
        public PurchaseVerifier getPurchaseVerifier() {
            return new ApiPurchaseVerifier();
        }

        @NonNull
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

    @Override
    public void onCreate(){

        // Possible XWalk fix
        XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW, false);

        configureJobManager();
        pluginManager = new PluginManager();
        preferences = getSharedPreferences("baker.app", 0);
        if(Configuration.isStandaloneMode()) {
            issueCollection = new LocalIssueCollection();
        }else{
            issueCollection = new RemoteIssueCollection();
        }
        licenceManager = new LicenceManager();
    }

    private void configureJobManager() {
        com.path.android.jobqueue.config.Configuration configuration = new com.path.android.jobqueue.config.Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";
                    @Override
                    public boolean isDebugEnabled() {
                        return getResources().getBoolean(R.bool.debug_mode);
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(0)
                .maxConsumerCount(5)
                .loadFactor(1)
                .consumerKeepAlive(30)
                .build();
        jobManager = new JobManager(this, configuration);
    }

    // Getters

    public JobManager getJobManager() {
        return jobManager;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public IssueCollection getIssueCollection() {
        return issueCollection;
    }

    public LicenceManager getLicenceManager() {
        return licenceManager;
    }

    public Checkout getCheckout() {
        if(checkout == null && !Configuration.isStandaloneMode()) {
            checkout = Checkout.forApplication(billing, Products.create().add(IN_APP, ((RemoteIssueCollection) issueCollection).getIssueProductIds()).add(SUBSCRIPTION, Configuration.getSubscriptionProductIds()));
            checkout.start();
        }
        return checkout;
    }

    public void setApplicationMode(int applicationMode) {
        this.applicationMode = applicationMode;
    }
    public int getApplicationMode() {
        return this.applicationMode;
    }

    // Helper methods

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) instance.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni != null) {
            BakerApplication.getInstance().setApplicationMode(BakerApplication.APPLICATION_MODE_ONLINE);
            return true;
        }else{
            BakerApplication.getInstance().setApplicationMode(BakerApplication.APPLICATION_MODE_OFFLINE);
            return false;
        }
    }

    public int getVersion() {
        try {
            PackageInfo packageInfo = instance.getPackageManager().getPackageInfo(instance.getPackageName(), 0);
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

    public static BakerApplication getInstance() {
        return instance;
    }

}
