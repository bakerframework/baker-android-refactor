/**
 * Copyright (c) 2013-2014. Francisco Contreras, Holland Salazar.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the Baker Framework nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
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
package com.bakerframework.baker.settings;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import com.bakerframework.baker.BakerApp;
import com.bakerframework.baker.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class Configuration {

    // Book preferences
    public final static String BOOK_JSON_KEY = "com.bakerframework.baker.BOOK_JSON_KEY";

    // Issue preferences
    public final static String ISSUE_NAME = "com.bakerframework.baker.ISSUE_NAME";
    public final static String ISSUE_STANDALONE = "com.bakerframework.baker.ISSUE_STANDALONE";
    public final static String ISSUE_RETURN_TO_SHELF = "com.bakerframework.baker.ISSUE_RETURN_TO_SHELF";
    public final static String ISSUE_ENABLE_BACK_NEXT_BUTTONS = "com.bakerframework.baker.ISSUE_ENABLE_BACK_NEXT_BUTTONS";
    public final static String ISSUE_ENABLE_DOUBLE_TAP = "com.bakerframework.baker.ISSUE_ENABLE_DOUBLE_TAP";
    public final static String ISSUE_ENABLE_TUTORIAL = "com.bakerframework.baker.ISSUE_ENABLE_TUTORIAL";

    // Global preferences
    public final static String PREF_REGISTRATION_ID = "com.bakerframework.baker.REGISTRATION_ID";
    public final static String PREF_DOWNLOAD_IN_PROGRESS = "com.bakerframework.baker.DOWNLOAD_IN_PROGRESS";
    public final static String PREF_APP_VERSION = "com.bakerframework.baker.APP_VERSION";
    public final static String PREF_FIRST_TIME_RUN = "com.bakerframework.baker.PREF_FIRST_TIME_RUN";

    // Settings preferences
    public static final String PREF_RECEIVE_NOTIFICATIONS = "pref_receive_notifications";
    public static final String PREF_RECEIVE_NOTIFICATIONS_DOWNLOAD = "pref_receive_notifications_download";
    public static final String PREF_RECEIVE_NOTIFICATIONS_DOWNLOAD_ONLY_WIFI = "pref_receive_notifications_download_only_wifi";

    // Logging
    private static final String tag = "BakerConfiguration";

    // File and directory handling settings
    public static final String MAGAZINES_FILES_DIR = "magazines";

    /**
     * Empty constructor not to be used since the class is utils only.
     */
	private Configuration() {}

    public static String getManifestUrl() {
        return BakerApp.getAppContext().getString(R.string.newstand_manifest_url)
            .replace(":app_id", BakerApp.getAppContext().getString(R.string.app_id))
            .replace(":device_type", "ANDROID")
            .replace(":user_id", getUserId());
    }

    // Tries to use external storage, if not available then fallback to internal.
    public static String getFilesDirectory() {
        String filesPath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File externalFilesDirectory = BakerApp.getAppContext().getExternalFilesDir("");
            if (null != externalFilesDirectory) {
                filesPath = externalFilesDirectory.getPath();
            } else {
                filesPath = BakerApp.getAppContext().getFilesDir().getPath();
            }
        } else {
            filesPath = BakerApp.getAppContext().getFilesDir().getPath();
        }
        return filesPath;
    }

    public static boolean getPrefFirstTimeRun() {

        // Always return false if the tutorial is disabled
        if(!BakerApp.getAppContext().getResources().getBoolean(R.bool.ut_enable_tutorial)) { return false; }

        // Check and update PREF_FIRST_TIME_RUN preference
        boolean firstTimeRun = BakerApp.getPreferenceBoolean(PREF_FIRST_TIME_RUN, true);
        if(firstTimeRun) {
            BakerApp.setPreferenceBoolean(PREF_FIRST_TIME_RUN, false);
        }

        return firstTimeRun;
    }

    public static String getUserId() {

        // Get saved settings
        String userId = BakerApp.getPreferenceString("user_id");
        if(userId == null) {

            // Getting the user main account
            AccountManager manager = AccountManager.get(BakerApp.getAppContext());
            Account[] primaryAccounts = manager.getAccountsByType("com.google");
            Account[] secondaryAccounts = manager.getAccounts();
            // Check for other accounts
            if (primaryAccounts.length > 0) {
                // Use main account
                userId = primaryAccounts[0].type + "_" + primaryAccounts[0].name;
            }else if (secondaryAccounts.length > 0) {
                // Use other system account
                userId = secondaryAccounts[0].type + "_" + secondaryAccounts[0].name;
            }else{
                // Use android id
                userId = Settings.Secure.getString(BakerApp.getAppContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            }

            // Save user id
            BakerApp.setPreferenceString("user_id", userId);

        }

        return userId;
    }

	public static String getMagazinesDirectory() {
		return getFilesDirectory() + File.separator + Configuration.MAGAZINES_FILES_DIR;
	}

    /* Gets the absolute cache dir for accessing files */
    public static String getCacheDirectory() {
        String cachePath;

        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {

            Log.d(tag, "EXTERNAL STORAGE IS MOUNTED FOR CACHE.");

            File externalCacheDirectory = BakerApp.getAppContext().getExternalCacheDir();
            if (null != externalCacheDirectory) {
                Log.d(tag, "USING EXTERNAL STORAGE FOR CACHE.");
                cachePath = externalCacheDirectory.getPath();
                Log.d(tag, "EXTERNAL PATH TO USE FOR CACHE: " + cachePath);
            } else {
                Log.d(tag, "USING INTERNAL STORAGE FOR CACHE.");
                cachePath = BakerApp.getAppContext().getCacheDir().getPath();
            }
        } else {
            Log.d(tag, "EXTERNAL STORAGE IS *NOT* MOUNTED FOR CACHE.");
            cachePath = BakerApp.getAppContext().getCacheDir().getPath();
        }
        return cachePath;
    }

    public static boolean connectionIsWiFi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) BakerApp.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return networkInfo.isConnected();
    }
	
	public static boolean hasNetworkConnection() {
		ConnectivityManager cm = (ConnectivityManager) BakerApp.getAppContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static Map<String, String> splitUrlQueryString(URL url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String query = url.getQuery();
        Log.d(tag, "URL QUERY RAW: " + query);
        String[] pairs = query.split("&");
        Log.d(tag, "URL QUERY PAIRS COUNT: " + pairs.length);
        if (pairs.length > 0) {
            for (String pair : pairs) {
                Log.d(tag, "SPLITTING URL QUERY PAIR " + pair);
                int idx = pair.indexOf("=");
                if (idx > -1) {
                    query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            }
        }
        return query_pairs;
    }

    public static boolean deleteDirectory(final String path) {
        File directory = new File(path);

        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files == null) {
                return true;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file.getPath());
                } else {
                    if(!file.delete()) {
                        return false;
                    }
                }
            }
        } else {
            return true;
        }

        return (directory.delete());
    }

}
