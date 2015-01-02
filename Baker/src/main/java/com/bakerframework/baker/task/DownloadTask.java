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

import android.os.AsyncTask;
import android.util.Log;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Long, String> {

    // Task management
    private DownloadTaskDelegate delegate;
    private String url;
    private File file;

    // Download handling
    private boolean downloading = false;
    private HttpURLConnection connection;
    private String errorCause;

    // Helper variables
    private String tag;

    public DownloadTask(DownloadTaskDelegate delegate, final String url, final File file) {

        // Task management
        this.delegate = delegate;
        this.url = url;
        this.file = file;

        // Helper variables
        this.tag = this.getClass().toString();
    }

    // Main routine

    @Override
    protected String doInBackground(String... params) {
        Log.d(tag, "DOWNLOADING FILE: " + url);
        String result;
        downloading = true;

        // Create target file
        if(!this.createTargetFile()) {
            errorCause = "Unable to save the file.";
            return "ERROR";
        }

        // Download file
        if(this.download()) {
            errorCause = "Unable to download the file.";
            result = "SUCCESS";
        }else{
            result = "ERROR";
        }

        // Delete target file on error
        if(result.equals("ERROR")) {
            this.deleteTargetFile();
        }

        return result;
    }

    // Getters

    public boolean isDownloading() {
        return downloading;
    }

    public String getErrorCause() {
        return errorCause;
    }

    public File getFile() {
        return file;
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        delegate.onDownloadProgress(this, progress[0], progress[1], progress[2]);
    }

    @Override
    protected void onPostExecute(String result) {
        if(result.equals("SUCCESS")) {
            delegate.onDownloadComplete(this, getFile());
        }else{
            delegate.onDownloadFailed(this);
        }
    }

    // Helper methods

    private boolean createTargetFile() {

        // Create directory structure
        if(!file.getParentFile().exists() || !file.getParentFile().isDirectory()) {
            file.getParentFile().mkdirs();
        }
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }
        return file.exists();
    }

    private boolean deleteTargetFile() {
        // Create directory structure
        if(file != null && file.exists()) {
            file.delete();
            return true;
        }else{
            return false;
        }
    }

    private boolean download() {

        try {

            // Prepare Download
            errorCause = null;
            connection = null;
            URL url = new URL(this.url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(true);

            // Prepare streams
            long totalBytes = connection.getContentLength();
            InputStream inputStream = connection.getInputStream();
            OutputStream output = new FileOutputStream(file);

            // Prepare iterators

            // Transfer variables
            long bytesSoFar = 0;
            long progress;
            int bytesRead;
            byte[] buffer = new byte[4096];

            // Download stream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // Check if the task was cancelled
                if (isCancelled()) {
                    if(file.exists() && file.isFile()) { file.delete(); }
                    break;
                }
                // Check for read bytes
                if (bytesRead > 0) {
                    // Update progress
                    if (totalBytes != -1) {
                        bytesSoFar = bytesSoFar + bytesRead;
                        progress = (long) ((float) bytesSoFar / totalBytes * 100);
                        publishProgress(progress, bytesSoFar, totalBytes);
                    }
                    // Write to file
                    output.write(buffer, 0, bytesRead);
                }
            }

            // Close output
            output.close();
        } catch (ConnectException e) {
            errorCause = BakerApplication.getInstance().getString(R.string.download_task_error_connect);
            return false;
        } catch (MalformedURLException e) {
            errorCause = BakerApplication.getInstance().getString(R.string.download_task_error_malformed_url);
            return false;
        } catch (FileNotFoundException e) {
            errorCause = BakerApplication.getInstance().getString(R.string.download_task_error_file_not_found);
            return false;
        } catch (IOException e) {
            errorCause = BakerApplication.getInstance().getString(R.string.download_task_error_io);
            return false;
        } finally {
            if(connection != null) { connection.disconnect(); }
            downloading = false;
        }

        if(isCancelled()) {
            errorCause = BakerApplication.getInstance().getString(R.string.download_task_error_cancelled);
            return false;
        }else{
            return true;
        }

    }

}
