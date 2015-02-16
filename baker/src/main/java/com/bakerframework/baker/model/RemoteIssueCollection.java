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

import android.support.annotation.NonNull;
import android.util.Log;

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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;

import static org.solovyev.android.checkout.ProductTypes.IN_APP;
import static org.solovyev.android.checkout.ProductTypes.SUBSCRIPTION;

public class RemoteIssueCollection implements IssueCollection {

    private final HashMap<String, Issue> issueMap;
    private List<String> categories;

    private List<Sku> subscriptionSkus;

    // Tasks management
    private DownloadManifestJob downloadManifestJob;
    private FetchPurchasesJob fetchPurchasesJob;

    // Data Processing
    final String JSON_ENCODING = "utf-8";
    final SimpleDateFormat SDF_INPUT = new SimpleDateFormat(BakerApplication.getInstance().getString(R.string.format_input_date), Locale.US);
    final SimpleDateFormat SDF_OUTPUT = new SimpleDateFormat(BakerApplication.getInstance().getString(R.string.format_output_date), Locale.US);

    // Categories
    public static final String ALL_CATEGORIES_STRING = "All Categories";

    // Billing
    @NonNull
    private Inventory inventory;

    public RemoteIssueCollection() {
        // Initialize issue map
        issueMap = new HashMap<>();
        EventBus.getDefault().register(this);
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<Sku> getSubscriptionSkus() {
        return subscriptionSkus;
    }

    public List<String> getIssueProductIds() {
        List<String> issueProductIdList = new ArrayList<>();
        for(Issue issue : getIssues()) {
            if(issue.getProductId() != null && !issue.getProductId().equals("")) {
                issueProductIdList.add(issue.getProductId());
            }
        }
        return issueProductIdList;
    }

    public List<Issue> getIssues() {
        if(isLoading() || issueMap == null) {
            return new ArrayList<>();
        }else{
            return new ArrayList<>(issueMap.values());
        }
    }

    public Issue getIssueBySku(Sku sku) {
        return getIssueByProductId(sku.id);
    }

    public Issue getIssueByProductId(String productId) {
        for(Issue issue : getIssues()) {
            if(issue.getProductId() != null && issue.getProductId().equals(productId)) {
                return issue;
            }
        }
        return null;
    }

    public boolean isLoading() {
        return (downloadManifestJob != null && !downloadManifestJob.isCompleted()) || (fetchPurchasesJob != null && !fetchPurchasesJob.isCompleted());
    }

    // Reload data from backend
    public void load() {
        if (!isLoading()) {
            if (BakerApplication.getInstance().isNetworkConnected()) {
                // Online Mode: Reload issue collection
                downloadManifestJob = new DownloadManifestJob(Configuration.getManifestUrl(), getCachedFile());
                BakerApplication.getInstance().getJobManager().addJobInBackground(downloadManifestJob);
            }else if(isCacheAvailable()) {
                processManifestFile(getCachedFile());
            }else{
                EventBus.getDefault().post(new IssueCollectionErrorEvent(new Exception("No cached file available")));
            }
        }
    }

    public void processManifestFileFromCache() {

    }

    private void processManifestFile(File file)  {

        try {

            // Create issues
            processJson(FileHelper.getJsonArrayFromFile(file));

            // Process categories
            categories = extractAllCategories();

            // you only need this if this activity needs information about purchases/SKUs
            if(BakerApplication.getInstance().isNetworkConnected()) {
                inventory = BakerApplication.getInstance().getCheckout().loadInventory();
                inventory.whenLoaded(new InventoryLoadedListener());
                inventory.load();
            }

            // Instantly trigger load event
            EventBus.getDefault().post(new IssueCollectionLoadedEvent());

        } catch (JSONException e) {
            Log.e(this.getClass().getName(), "processing error (invalid json): " + e);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "processing error (buffer error): " + e);
        } catch (ParseException e) {
            Log.e(this.getClass().getName(), "processing error (parse error): " + e);
        }

    }

    private void processJson(final JSONArray jsonArray) throws JSONException, ParseException, UnsupportedEncodingException {
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
            Date issueObjDate = jsonObjDate(json.getString("date"));
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
            issue.setObjDate(issueObjDate);
            issue.setCover(issueCover);
            issue.setUrl(issueUrl);
            issue.setSize(issueSize);

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

    private String jsonDate(String value) throws ParseException {
        return SDF_OUTPUT.format(SDF_INPUT.parse(value));
    }

    private Date jsonObjDate(String value) throws ParseException {
        return SDF_INPUT.parse(value);
    }

    private String jsonString(String value) throws UnsupportedEncodingException {
        if(value != null) {
            return new String(value.getBytes(JSON_ENCODING), JSON_ENCODING);
        }else{
            return null;
        }
    }

    private String getCachedPath() {
        return Configuration.getCacheDirectory() + File.separator + BakerApplication.getInstance().getString(R.string.path_shelf);
    }

    private File getCachedFile() {
        return new File(getCachedPath());
    }

    public boolean isCacheAvailable() {
        return getCachedFile().exists() && getCachedFile().isFile();
    }

    public void updatePrices(Inventory.Products inventoryProducts, List<String> productIds) {

        // Update google-play subscriptions
        if(inventoryProducts != null) {
            boolean hasSubscription = false;
            subscriptionSkus = new ArrayList<>();
            final Inventory.Product subscriptionProductCollection = inventoryProducts.get(SUBSCRIPTION);
            if (subscriptionProductCollection.supported) {
                for (Sku sku : subscriptionProductCollection.getSkus()) {
                    subscriptionSkus.add(sku);
                }
            }

            // Update google-play purchased issues
            final Inventory.Product inAppProductCollection = inventoryProducts.get(IN_APP);
            if (inAppProductCollection.supported) {
                // Update issue prices
                for (Sku sku : inAppProductCollection.getSkus()) {
                    Issue issue = getIssueBySku(sku);
                    if(issue != null) {
                        // Check for subscription
                        issue.setPurchased(inAppProductCollection.isPurchased(sku));
                        issue.setSku(sku);
                    }
                }
            } else {
                Log.e(getClass().getName(), "Error: " + R.string.err_purchase_not_possible);
            }
        }

        // Update backend-purchased issues
        if(productIds != null) {
            for (String productId : productIds) {
                Issue issue = getIssueByProductId(productId);
                if(issue != null) {
                    issue.setPurchased(true);
                }
            }
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

    private class InventoryLoadedListener implements Inventory.Listener {
        @Override
        public void onLoaded(@NonNull Inventory.Products inventoryProducts) {
            // Load existing purchases from backend
            fetchPurchasesJob = new FetchPurchasesJob(Configuration.getManifestUrl());
            BakerApplication.getInstance().getJobManager().addJobInBackground(fetchPurchasesJob);
        }
    }

    // @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DownloadManifestCompleteEvent event) {
        processManifestFile(getCachedFile());
    }

    // @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DownloadManifestErrorEvent event) {
        Log.i("IssueCollection", "DownloadManifestErrorEvent");
        if(isCacheAvailable()) {
            processManifestFile(getCachedFile());
        }else{
            EventBus.getDefault().post(new IssueCollectionErrorEvent(new Exception("No cached file available")));
        }
    }

    // @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(FetchPurchasesCompleteEvent event) {

        // Set purchased issues
        updatePrices(inventory.getProducts(), event.getFetchPurchasesResponse().issues);

        // Trigger issues loaded event
        EventBus.getDefault().post(new IssueCollectionLoadedEvent());

    }


    // @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(FetchPurchasesErrorEvent event) {

        // Set purchased issues
        updatePrices(inventory.getProducts(), null);

        // Trigger issues loaded event
        EventBus.getDefault().post(new IssueCollectionLoadedEvent());

    }

    public Inventory getInventory() {
        return inventory;
    }

}
