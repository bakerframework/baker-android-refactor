/**
 * Copyright (c) 2013-2014. Francisco Contreras, Holland Salazar.
 * Copyright (c) 2015. Tobias Strebitzer, Francisco Contreras, Holland Salazar.
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * <p/>
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
 */
package com.bakerframework.baker.handler;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadHandler {
    private final String url;
    private File targetFile;
    private boolean completed = false;
    private int percentComplete;
    private HttpURLConnection connection = null;
    private long totalBytes;
    private InputStream inputStream;

    public DownloadHandler(String url) {
        this.url = url;
        this.percentComplete = 0;
    }

    public void download(File targetFile) throws Exception {
        this.targetFile = targetFile;
        try {
            createTargetFile();
            prepareDownload();
            downloadToFile();
        }catch (ConnectException e) {
            throw new Exception(BakerApplication.getInstance().getString(R.string.err_download_task_connect));
        }catch (MalformedURLException e) {
            throw new Exception(BakerApplication.getInstance().getString(R.string.err_download_task_malformed_url));
        }catch (FileNotFoundException e) {
            throw new Exception(BakerApplication.getInstance().getString(R.string.err_download_task_file_not_found));
        }catch (IOException e) {
            throw new Exception(BakerApplication.getInstance().getString(R.string.err_download_task_io));
        }finally {
            this.cleanup();
        }
    }

    public String read() throws Exception {
        try {
            prepareDownload();
            return readAsString();
        }catch (ConnectException e) {
            throw new Exception(BakerApplication.getInstance().getString(R.string.err_download_task_connect));
        }catch (MalformedURLException e) {
            throw new Exception(BakerApplication.getInstance().getString(R.string.err_download_task_malformed_url));
        }catch (FileNotFoundException e) {
            throw new Exception(BakerApplication.getInstance().getString(R.string.err_download_task_file_not_found));
        }catch (IOException e) {
            throw new Exception(BakerApplication.getInstance().getString(R.string.err_download_task_io));
        }finally {
            this.cleanup();
        }
    }

    public void cancel() {
        this.completed = true;
    }

    public boolean isCompleted() {
        return completed;
    }

    private void prepareDownload() throws Exception {

        // Prepare Download
        URL url = new URL(this.url);
        connection = (HttpURLConnection) url.openConnection();
        connection.setUseCaches(true);

        // Prepare streams
        totalBytes = connection.getContentLength();
        inputStream = connection.getInputStream();

    }

    private void downloadToFile() throws IOException {
        // Download to file
        OutputStream output = new FileOutputStream(targetFile);

        // Transfer variables
        long bytesSoFar = 0;
        int progress;
        int bytesRead;
        byte[] buffer = new byte[4096];

        // Download stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            // Check if the task was cancelled
            if (this.isCompleted()) {
                this.deleteTargetFile();
                break;
            }
            // Check for read bytes
            if (bytesRead > 0) {
                // Update progress
                if (totalBytes != -1) {
                    bytesSoFar = bytesSoFar + bytesRead;
                    progress = (int) ((float) bytesSoFar / totalBytes * 100);
                    if (progress > percentComplete) {
                        percentComplete = progress;
                        onDownloadProgress(percentComplete, bytesSoFar, totalBytes);
                    }
                }
                // Write to file
                output.write(buffer, 0, bytesRead);
            }
        }

        // Close output
        output.close();
    }

    private String readAsString() throws IOException {

        // Download to string
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader( new InputStreamReader(inputStream));
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

    private boolean deleteTargetFile() {
        if(targetFile != null && targetFile.exists()) {
            return targetFile.delete();
        }else{
            return false;
        }
    }

    private void createTargetFile() throws Exception {
        // Create directory structure
        if(!targetFile.getParentFile().exists() || !targetFile.getParentFile().isDirectory()) {
            targetFile.getParentFile().mkdirs();
        }
        if(!targetFile.exists()) {
            targetFile.createNewFile();
        }
        if(!targetFile.exists()) {
            throw new Exception("Unable to create target file");
        }
    }

    private void cleanup() {
        if(connection != null) {
            connection.disconnect();
            connection = null;
        }
    }

    public void onDownloadProgress(int percentComplete, long bytesSoFar, long totalBytes) {

    }

}
