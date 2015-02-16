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
package com.bakerframework.baker.model;

import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;
import com.bakerframework.baker.events.DownloadManifestCompleteEvent;
import com.bakerframework.baker.events.DownloadManifestErrorEvent;
import com.bakerframework.baker.events.FetchPurchasesCompleteEvent;
import com.bakerframework.baker.events.FetchPurchasesErrorEvent;
import com.bakerframework.baker.events.IssueCollectionErrorEvent;
import com.bakerframework.baker.events.IssueCollectionLoadedEvent;
import com.bakerframework.baker.helper.FileHelper;
import com.bakerframework.baker.jobs.DownloadManifestJob;
import com.bakerframework.baker.jobs.FetchPurchasesJob;
import com.bakerframework.baker.settings.Configuration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.Sku;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;

import static org.solovyev.android.checkout.ProductTypes.IN_APP;
import static org.solovyev.android.checkout.ProductTypes.SUBSCRIPTION;

public class LocalIssueCollection implements IssueCollection {

    private final HashMap<String, Issue> issueMap;
    private List<String> categories;

    // Data Processing
    final String JSON_ENCODING = "utf-8";
    final SimpleDateFormat SDF_INPUT = new SimpleDateFormat(BakerApplication.getInstance().getString(R.string.format_input_date), Locale.US);
    final SimpleDateFormat SDF_OUTPUT = new SimpleDateFormat(BakerApplication.getInstance().getString(R.string.format_output_date), Locale.US);

    // Categories
    public static final String ALL_CATEGORIES_STRING = "All Categories";

    public LocalIssueCollection() {
        issueMap = new HashMap<>();
        // EventBus.getDefault().register(this);
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<Issue> getIssues() {
        if(issueMap == null) {
            return new ArrayList<>();
        }else{
            return new ArrayList<>(issueMap.values());
        }
    }

    // Reload data from backend
    public void load() {
        ArrayList<String> issues;
        if (Configuration.shouldReadStandaloneFromCustomDirectory()) {
            // @TODO: Implement expansion file download
            /*
            if (this.expansionFileExists()) {
                Log.d(this.getClass().getName(), "The expansion file exists.");
                File directory = new File(Configuration.getMagazinesDirectory());
                if (directory.exists() && (directory.list().length > 0)) {
                    if (this.getExtractionFinished()) {
                        Log.d(this.getClass().getName(), "Magazines directory not empty and extraction finished.");
                        this.getValidIssuesFromSharedStorage();
                    } else {
                        Log.d(this.getClass().getName(), "Magazines directory not empty but the extraction did not finished. Trying again.");
                        this.extractFromExpansionFile();
                    }
                } else {
                    Log.d(this.getClass().getName(), "No magazines detected on the magazines directory.");
                    this.saveExtractionFinished(false);
                    this.extractFromExpansionFile();
                }
            } else {
                Log.d(this.getClass().getName(), "The expansion file does not exist.");
                this.downloadExpansionFile();
            }
            */
        }else{
            issues = this.getValidIssuesAssets();
            readStandaloneIssues(issues);

            // Trigger issues loaded event
            EventBus.getDefault().post(new IssueCollectionLoadedEvent());
        }
    }

    private void readStandaloneIssues(final ArrayList<String> issues) {
        JSONArray jsonArray = new JSONArray();
        for (String issueFileName : issues) {
            Log.d(this.getClass().getName(), "The file is: " + issueFileName);
            jsonArray.put(this.getIssueData(issueFileName));
        }
        try {
            this.processJson(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getIssueData(final String issueName) {
        String books;
        boolean fromAssets = !BakerApplication.getInstance().getResources().getBoolean(R.bool.sa_read_from_custom_directory);
        final String bookJson = "book.json";
        JSONObject result = new JSONObject();
        BufferedReader reader = null;
        try {
            result.put("name", issueName);
            AssetManager assetManager = BakerApplication.getInstance().getAssets();
            String bookJsonPath = issueName.concat(File.separator).concat(bookJson);
            if (fromAssets) {
                books = BakerApplication.getInstance().getString(R.string.sa_books_directory).concat(File.separator).concat(bookJsonPath);
                reader = new BufferedReader(new InputStreamReader(assetManager.open(books)));
            } else {
                books = Configuration.getMagazinesDirectory().concat(File.separator).concat(bookJsonPath);
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(books)));
            }

            String line = "";
            StringBuilder jsonString = new StringBuilder();
            do {
                jsonString.append(line);
                line = reader.readLine();
            } while (line != null);

            Log.d(this.getClass().getName(), "The book.json read was: " + jsonString.toString());
            JSONObject jsonRaw = new JSONObject(jsonString.toString());
            result.put("title", jsonRaw.getString("title"));
            result.put("url", jsonRaw.getString("url"));
            result.put("info", jsonRaw.getString("title"));
            result.put("cover", "file://" + (jsonRaw.has("cover") ? jsonRaw.getString("cover") : "cover.png"));
            result.put("date", jsonRaw.getString("date"));

        } catch (JSONException ex) {
            Log.e(this.getClass().getName(), "Error getting issue information from " + issueName, ex);
        } catch (IOException ex) {
            Log.e(this.getClass().getName(), "Error getting issue information from " + issueName, ex);
        } finally {
            try {
                reader.close();
            } catch (Exception ex) {
                //
            }
        }

        return result;
    }

    private ArrayList<String> getValidIssuesAssets() {
        final String path = Configuration.getStandaloneBooksDirectory();
        ArrayList<String> issues = new ArrayList<String>();
        try {
            AssetManager assetManager = BakerApplication.getInstance().getAssets();
            String assetList[] = assetManager.list(path);
            String fileName;
            for (String asset : assetList) {
                fileName = path.concat(File.separator).concat(asset);
                if (assetManager.list(fileName).length > 0) {
                    if (this.hasBookJson(fileName)) {
                        Log.d(this.getClass().getName(), "Valid issue found: " + fileName);
                        issues.add(asset);
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(this.getClass().getName(), "Error getting issues from assets", ex);
        }
        return issues;
    }

    private boolean hasBookJson(final String issuePath) {
        boolean result = false;
        final String bookJson = "book.json";
        AssetManager assetManager = BakerApplication.getInstance().getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(issuePath.concat(File.separator).concat(bookJson));
            result = true;
        } catch (Exception ex) {
            result = false;
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.e(this.getClass().getName(), "Error opening the book.json for " + issuePath);
            }
        }
        return result;
    }

    private void processJson(final JSONArray jsonArray) throws JSONException, UnsupportedEncodingException {
        JSONObject json;
        JSONArray jsonCategories;
        List<String> categories;
        List<String> issueNameList = new ArrayList<>();

        // Loop through issues
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            json = new JSONObject(jsonArray.getString(i));

            // Get issue data from json
            String issueName = jsonString(json.getString("name"));
            String issueProductId = json.isNull("product_id") ? null : jsonString(json.getString("product_id"));
            String issueTitle = jsonString(json.getString("title"));
            String issueInfo = jsonString(json.getString("info"));
            String issueDate = jsonDate(json.getString("date"));
            String issueCover = jsonString(json.getString("cover"));
            String issueUrl = jsonString(json.getString("url"));
            int issueSize = json.has("size") ? json.getInt("size") : 0;

            Issue issue;
            if(issueMap.containsKey(issueName)) {
                // Get issue from issue map
                issue = issueMap.get(issueName);
                // Flag fields for update
                if(!issue.getCover().equals(issueCover)) {
                    issue.setCoverChanged(true);
                }
                if(!issue.getUrl().equals(issueUrl)) {
                    issue.setUrlChanged(true);
                }
            }else{
                // Create new issue and store in issue map
                issue = new Issue(issueName);
                issueMap.put(issueName, issue);
            }

            // Set issue data
            issue.setTitle(issueTitle);
            issue.setProductId(issueProductId);
            issue.setInfo(issueInfo);
            issue.setDate(issueDate);
            issue.setCover(issueCover);
            issue.setUrl(issueUrl);
            issue.setSize(issueSize);
            issue.setStandalone(true);

            // Set categories
            if(json.has("categories")) {
                jsonCategories = json.getJSONArray("categories");
                categories = new ArrayList<>();
                for (int j = 0; j < jsonCategories.length(); j++) {
                    categories.add(jsonCategories.get(j).toString());
                }
                issue.setCategories(categories);
            }else{
                issue.setCategories(new ArrayList<String>());
            }

            // Add name to issue name list
            issueNameList.add(issueName);

        }

        // Get rid of old issues that are no longer in the manifest
        for(Issue issue : issueMap.values()) {
            if(!issueNameList.contains(issue.getName())) {
                issueMap.remove(issue);
            }
        }

    }

    // Helpers

    private String jsonDate(String value) {
        try {
            return SDF_OUTPUT.format(SDF_INPUT.parse(value));
        } catch (ParseException e) {
            return "";
        }
    }

    private String jsonString(String value) throws UnsupportedEncodingException {
        if(value != null) {
            return new String(value.getBytes(JSON_ENCODING), JSON_ENCODING);
        }else{
            return null;
        }
    }

    public List<String> extractAllCategories() {

        // Collect all categories from issues
        List<String> allCategories = new ArrayList<>();

        for(Issue issue : issueMap.values()) {
            for(String category : issue.getCategories()) {
                if(allCategories.indexOf(category) == -1) {
                    allCategories.add(category);
                }
            }
        }

        // Sort categories
        Collections.sort(allCategories);

        // Append all categories item
        allCategories.add(0, ALL_CATEGORIES_STRING);

        return allCategories;
    }

    public List<Issue> getDownloadingIssues() {
        List<Issue> downloadingIssues = new ArrayList<>();
        for (Issue issue : issueMap.values()) {
            if(issue.isDownloading()) {
                downloadingIssues.add(issue);
            }
        }
        return downloadingIssues;
    }

    public void cancelDownloadingIssues(final List<Issue> downloadingIssues) {
        for (Issue issue : downloadingIssues) {
            if(issue.isDownloading()) {
                issue.cancelDownloadJob();
            }
        }
    }

    public Issue getIssueByName(String issueName) {
        return issueMap.get(issueName);
    }

    /*
    @Override
    public void onEventMainThread(Object event) {
        Log.d("RemoteIssueCollection", "onEventMainThread called");
    }
    */

}
