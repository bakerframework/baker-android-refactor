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
package com.bakerframework.baker.task;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import com.bakerframework.baker.R;
import com.bakerframework.baker.client.TaskMandator;
import com.bakerframework.baker.model.BookJson;
import com.bakerframework.baker.model.Issue;
import com.bakerframework.baker.settings.Configuration;

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
		
		String rawJson;
		try {
            if ("STANDALONE".equals(params[0])) {
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
