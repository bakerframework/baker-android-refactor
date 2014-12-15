package com.bakerframework.baker.workers;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import com.bakerframework.baker.R;
import com.bakerframework.baker.client.TaskMandator;
import com.bakerframework.baker.model.BookJson;
import com.bakerframework.baker.model.Issue;
import com.bakerframework.baker.settings.Configuration;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class BookJsonParserTask extends AsyncTask<String, Long, BookJson> {

	private String magazinesDirectory;
	
	private Issue issue;

    private Context context;

    private TaskMandator mandator;

    private int taskId;

	public BookJsonParserTask() {
		this.magazinesDirectory = Configuration.getMagazinesDirectory();
	}
	
	public BookJsonParserTask(Context _context, Issue _issue, TaskMandator _mandator, int _taskId) {
        this.context = _context;
        this.issue = _issue;
        this.mandator = _mandator;
        this.taskId = _taskId;
        this.magazinesDirectory = Configuration.getMagazinesDirectory();
	}

	@Override
	protected BookJson doInBackground(String... params) {
		BookJson result  = null;
		
		String rawJson = "";
		try {
            if ("ONLINE".equals(params[0])) {
                Log.d(this.getClass().toString(), "Will parse the BookJson from the Live URL: " + this.issue.getLiveUrl());

                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(this.issue.getLiveUrl() + "/" + this.context.getString(R.string.book));
                HttpResponse response = httpClient.execute(httpGet);
                if (null != response) {

                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        rawJson = EntityUtils.toString(response.getEntity());
                        Log.d(this.getClass().toString(), "Get request for book.json succeeded: " + rawJson);
                    } else {
                        Log.e(this.getClass().toString(), "Bad response when obtaining the book.json: " + statusCode);
                    }
                } else {
                    Log.e(this.getClass().toString(), "The response is NULL when obtaining the book.json.");
                }
            } else if ("STANDALONE".equals(params[0])) {
                Log.d(this.getClass().getName(), "Will parse the BookJson from the assets directory." );

                AssetManager assetManager = this.context.getAssets();
                String bookJsonPath = this.context.getString(R.string.sa_books_directory)
                        .concat(File.separator)
                        .concat(this.issue.getName())
                        .concat(File.separator)
                        .concat("book.json");
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(assetManager.open(bookJsonPath)));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                rawJson = sb.toString();

                Log.d(this.getClass().toString(), "Book.json read from file: " + rawJson);

                reader.close();

            } else {
                Log.d(this.getClass().getName(), "Will parse the BookJson from the file system." );

                String workingDir = this.magazinesDirectory + File.separator;
                File book = new File(workingDir + params[0] + File.separator + this.context.getString(R.string.book));

                FileInputStream input = new FileInputStream(book);

                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                rawJson = sb.toString();

                Log.d(this.getClass().toString(), "Book.json read from file: " + rawJson);

                input.close();
            }
		    
		    boolean valid = this.validateJson(rawJson);
		    
		    if (valid) {
			    Log.d(this.getClass().toString(), "Book.json is valid.");
		    	result = new BookJson();
                if (this.issue.getLiveUrl() != null) {
                    result.setLiveUrl(this.issue.getLiveUrl() + "/");
                }
		    	result.fromJson(rawJson);
		    	result.setMagazineName(this.issue.getName());
		    } else {
			    Log.d(this.getClass().toString(), "Book.json is NOT valid.");
		    }
		} catch (Exception ex) {
            Log.e(this.getClass().getName(), "Error parsing the book.json.", ex);
			result = null;
		}	    
		
		return result;
	}

	private boolean validateJson(final String rawJson) {
		boolean result = true;
		String required[] = {"contents"};
		
		try {
			JSONObject json = new JSONObject(rawJson);
			
			for (String property : required) {
				if (!json.has(property)) {
					Log.e(this.getClass().toString(), "Property missing from json: " + property);
					result = false;
				}
			}
			
			if (json.has("contents")) {
				
				// If the contents is not array, this will result on an exception causing the book
				// to be invalid.
				JSONArray contents = new JSONArray(json.getString("contents"));
				if (contents.length() < 0) {
					result = false;
				}
			}
			
		} catch (JSONException e) {
			result = false;
			e.printStackTrace();
		}
		
		return result;
	}
	
	@Override
	protected void onProgressUpdate(Long... progress) {
	}

	@Override
	protected void onPostExecute(final BookJson result) {
        try {
            mandator.postExecute(taskId, result.getMagazineName(), result.toJSON().toString());
        } catch (JSONException ex) {
            mandator.postExecute(taskId, "", "");
        }

		//this.issue.setBookJson(result);
	}

}
