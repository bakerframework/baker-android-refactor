package com.bakerframework.baker.model;

import android.util.Log;

import com.bakerframework.baker.BakerApp;
import com.bakerframework.baker.R;
import com.bakerframework.baker.client.TaskMandator;
import com.bakerframework.baker.settings.Configuration;
import com.bakerframework.baker.workers.DownloadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Tobias Strebitzer <tobias.strebitzer@magloft.com> on 15/12/14.
 * http://www.magloft.com
 */
public class Manifest implements TaskMandator {

    private List<Issue> issues;
    private List<String> categories;

    // Tasks management
    private DownloadTask downloadManifestTask;
    private boolean isLoading = false;

    // Data Processing
    String JSON_ENCODING = "utf-8";
    SimpleDateFormat SDF_INPUT = new SimpleDateFormat(BakerApp.getAppContext().getString(R.string.inputDateFormat), Locale.US);
    SimpleDateFormat SDF_OUTPUT = new SimpleDateFormat(BakerApp.getAppContext().getString(R.string.outputDateFormat), Locale.US);

    // Categories
    public static final String ALL_CATEGORIES_STRING = "All Categories";

    // Event callbacks
    private ArrayList<ManifestListener> listeners = new ArrayList<>();

    // Constructor with file (instantly read json)
    public Manifest() {}

    public List<String> getCategories() {
        return categories;
    }

    public List<Issue> getIssues() {
        if(isLoading || issues == null) {
            return new ArrayList<>();
        }else{
            return issues;
        }
    }

    public boolean isLoading() {
        return isLoading;
    }

    // Set event listener
    public void setManifestListener (ManifestListener listener) {
        this.listeners.add(listener);
    }

    // Reload data from backend
    public void reload() {
        if(!isLoading) {
            isLoading = true;
            downloadManifestTask = new DownloadTask(this, 0, Configuration.getManifestUrl(), "shelf.json", Configuration.getCacheDirectory());
            downloadManifestTask.execute();
        }else{
            throw new RuntimeException("reload method invoked on Manifest while already downloading data");
        }
    }

    public void cancelLoading() {
        downloadManifestTask.cancel(true);
    }

    // Categories

    public void processCategories() {
        // Collect all categories from issues
        categories = new ArrayList<>();
        for(Issue issue : issues) {
            for(String category : issue.getCategories()) {
                if(categories.indexOf(category) == -1) {
                    categories.add(category);
                }
            }
        }

        // Sort categories
        Collections.sort(categories);

        // Append all categories item
        categories.add(0, ALL_CATEGORIES_STRING);
    }

    // Cache functionality

    private String getCachedPath() {
        return Configuration.getCacheDirectory() + File.separator + BakerApp.getAppContext().getString(R.string.shelf);
    }

    private File getCachedFile() {
        return new File(getCachedPath());
    }

    public boolean isCacheAvailable() {
        return getCachedFile().exists() && getCachedFile().isFile();
    }

    public void loadFromCache() {
        processManifestFile(getCachedFile());
    }

    @Override
    public void updateProgress(int taskId, Long... progress) {

    }

    @Override
    public void postExecute(int taskId, String... results) {

        // Update state
        isLoading = false;

        // Handle task result
        if(results[0].equals("SUCCESS")) {
            processManifestFile(new File(results[1]));
        }

    }

    // Data Handling
    private void processManifestFile(File file)  {

        try {

            // Create issues
            issues = processJson(new JSONArray(getStringFromFile(file)));

            // Process categories
            processCategories();

            // Trigger issues loaded event
            for (ManifestListener listener : listeners) {
                listener.onManifestLoaded();
            }

        } catch (FileNotFoundException e) {
            Log.e(this.getClass().getName(), "Manifest processing error (not found): " + e);
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), "Manifest processing error (invalid json): " + e);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Manifest processing error (buffer error): " + e);
        } catch (ParseException e) {
            Log.e(this.getClass().getName(), "Manifest processing error (parse error): " + e);
        }

    }

    private String getStringFromFile(File file) throws IOException {
        // Read file
        FileInputStream in = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        StringBuilder data = new StringBuilder("");
        while (in.read(buffer) != -1) {
            data.append(new String(buffer));
        }
        in.close();

        // Read json
        return data.toString();
    }

    private List<Issue> processJson(final JSONArray jsonArray) throws JSONException, ParseException, UnsupportedEncodingException {
        JSONObject json;
        JSONArray jsonCategories;
        List<String> categories;
        List<Issue> issueList = new ArrayList<>();

        // Loop through issues
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            json = new JSONObject(jsonArray.getString(i));

            // Prepare categories
            jsonCategories = json.getJSONArray("categories");
            categories = new ArrayList<>();
            for (int j = 0; j < jsonCategories.length(); j++) {
                categories.add(jsonCategories.get(j).toString());
            }

            // Create issue
            Issue issue = new Issue();
            issue.setName(new String(json.getString("name").getBytes(JSON_ENCODING), JSON_ENCODING));
            issue.setCategories(categories);
            issue.setTitle(new String(json.getString("title").getBytes(JSON_ENCODING), JSON_ENCODING));
            issue.setInfo(new String(json.getString("info").getBytes(JSON_ENCODING), JSON_ENCODING));
            issue.setDate(SDF_OUTPUT.format(SDF_INPUT.parse(json.getString("date"))));
            issue.setSize(json.has("size") ? json.getInt("size") : 0);
            issue.setCover(new String(json.getString("cover").getBytes(JSON_ENCODING), JSON_ENCODING));
            issue.setUrl(new String(json.getString("url").getBytes(JSON_ENCODING), JSON_ENCODING));

            issueList.add(issue);
        }

        return issueList;

    }

}
